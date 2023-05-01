package org.grego.recipeservice

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.jayway.jsonpath.{Configuration, JsonPath}
import net.minidev.json.JSONArray
import org.apache.commons.lang3.{RandomStringUtils, SystemUtils}
import org.apache.commons.text.StringSubstitutor
import org.apache.http.client.utils
import org.apache.http.client.utils.URIBuilder
import org.grego.recipeservice.controller.{BaseAppTest, RecipeController}
import org.grego.recipeservice.model.{Ingredient, Instruction, QuantitySpecifier, Recipe}
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.{Tag, Test, TestInstance}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.{MediaType, RequestEntity, ResponseEntity}
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.shaded.org.apache.commons.io.IOUtils
import org.testcontainers.utility.DockerImageName

import java.io.{BufferedInputStream, ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream, IOException, InputStream}
import java.net.{URI, URISyntaxException, URL}
import java.nio.file.{Files, Paths}
import java.security.cert.{Certificate, CertificateException, CertificateFactory}
import java.util
import java.util.{Collections, HashMap, LinkedHashMap, Optional}
import javafx.util.Pair
import java.security.{KeyStore, KeyStoreException, NoSuchAlgorithmException}

import scala.collection.mutable.ListBuffer

@SuppressWarnings(value = Array("scalastyle:magicnumber"))
@ExtendWith(Array(classOf[SpringExtension]))
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("IntegrationTests")
class RecipeServiceAppTest extends BaseAppTest {
  @Autowired
  var recipeController: RecipeController = _

  private val jsonPath = Configuration.defaultConfiguration.jsonProvider

  private final val recipe = Recipe.create("Test", "Test recipe",
    util.Arrays.asList(Ingredient.create("something", QuantitySpecifier.Unspecified, 0.0)),
    util.Arrays.asList(Instruction.create("Do something")))

  private final val includeHyperLinksParam = "include-hyper-links"


  /**
   * The configuration .yml file.
   */
  private val CONFIGURATION_YML_FILE = "application-test.yml"

  /**
   * The password for the keystore.
   */
  private val KEYSTORE_PASSWORD = RandomStringUtils.random(10, true, true)

  /**
   * Elasticsearch port.
   */
  private val ELASTICSEARCH_PORT = 9200

  /**
   * The version of Elasticsearch.
   */
  private val ELASTICSEARCH_VERSION = "8.6.2"

  private val ELASTICSEARCH_ARCH = if (SystemUtils.OS_ARCH == "aarch64") "arm64" else "amd64"

  /**
   * The docker image for Elasticsearch.
   */
  private val ELASTICSEARCH_IMAGE =
    s"docker.elastic.co/elasticsearch/elasticsearch:$ELASTICSEARCH_VERSION-$ELASTICSEARCH_ARCH"

  /**
   * Elasticsearch password.
   */
  private val ELASTICSEARCH_PASSWORD = RandomStringUtils.random(10, true, true)

//  /**
//   * Container to run test instance of Elasticsearch.
//   */
//  private var elasticsearchContainer: ElasticsearchContainer = {
//    val elasticsearchContainer = new ElasticsearchContainer(new DockerImageName(ELASTICSEARCH_IMAGE))
//      .withEnv("cluster.name", "integration-test-cluster")
//      .withPassword(ELASTICSEARCH_PASSWORD)
//
//    elasticsearchContainer.start()
//    val configurationMapping =
//      util.Map.of(
//        "elasticsearch.port", elasticsearchContainer.getMappedPort(ELASTICSEARCH_PORT),
//        "elasticsearch.password", ELASTICSEARCH_PASSWORD
//      )
//
//    writeConfigurationYmlFile(configurationMapping)
//
//    val certificates = new ListBuffer[Pair[String, Certificate]]
//
//    addElasticsearchCertificate(certificates, elasticsearchContainer)
//    createKeyStore(certificates.toList)
//
//    elasticsearchContainer
//  }

  @Test
  def testListRecipes(): Unit = {
    var recipeId: Integer = null

    try {
      val listRecipes = s"http://localhost:$port/recipes/list?$includeHyperLinksParam=true"

      verifyListRecipesSize(0)

      val listRecipesWithHyperLinksResponse =
        restTemplate.exchange(RequestEntity.get(listRecipes).build, classOf[String])
      val recipeListJson = jsonPath.parse(listRecipesWithHyperLinksResponse.getBody)
      val addRecipeUrl =
        JsonPath.read(recipeListJson, "$.actions[?(@.method == 'PUT')].href")
          .asInstanceOf[JSONArray].get(0).asInstanceOf[String]

      verifyUrlsMatch(listRecipes, JsonPath.read(recipeListJson,
        "$.links[*].href").asInstanceOf[JSONArray].get(0).asInstanceOf[String])
      assertTrue(addRecipeUrl.contains(includeHyperLinksParam + "=false"))

      val addRecipeResponse =
        restTemplate.exchange(RequestEntity.put(addRecipeUrl).accept(MediaType.APPLICATION_JSON).body(recipe),
          classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)
      verifyListRecipesSize(1)

      val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody)
      recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]
      assertNotNull(recipeId)
    } finally {
      if (recipeId != null) {
        val deleteRecipeResponse = recipeController.deleteRecipe(recipeId.longValue)

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }
    verifyListRecipesSize(0)
  }

  @Test
  def testAddRecipeWithoutHyperlinks(): Unit = {
    var recipeId: Integer = null

    try {
      val addRecipe = s"http://localhost:$port/recipes/add"

      verifyListRecipesSize(0)

      val addRecipeResponse =
        restTemplate.exchange(RequestEntity.put(addRecipe).accept(MediaType.APPLICATION_JSON).body(recipe),
          classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)

      val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]

      verifyRecipe(recipe, addRecipeJson)

      recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

      assertNotNull(recipeId)

      val getRecipeResponse =
        restTemplate.exchange(RequestEntity.get(s"http://localhost:$port/recipes/get/$recipeId").build,
          classOf[String])

      assertEquals(200, getRecipeResponse.getStatusCodeValue)
      verifyRecipe(recipe, jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])
    } finally {
      if (recipeId != null) {
        val deleteRecipeResponse = recipeController.deleteRecipe(recipeId.longValue())

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }

    verifyListRecipesSize(0)
  }

  @Test
  def testAddRecipeWithHyperlinks(): Unit = {
    var deleteRecipe: String = null

    try {
      val addRecipe = new URIBuilder(s"http://localhost:$port/recipes/add")
        .addParameter(includeHyperLinksParam, "true").build.toString

      verifyListRecipesSize(0)

      val addRecipeResponse =
        restTemplate.exchange(RequestEntity
          .put(addRecipe)
          .accept(MediaType.APPLICATION_JSON)
          .body(recipe), classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)

      val responseJson = jsonPath.parse(addRecipeResponse.getBody)
      val addRecipeJson = JsonPath.read(responseJson, "$.properties")
        .asInstanceOf[util.LinkedHashMap[String, Object]]

      verifyRecipe(recipe, addRecipeJson)

      val recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

      assertNotNull(recipeId)

      val getRecipe = s"http://localhost:$port/recipes/get/$recipeId"
      val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])

      assertEquals(200, getRecipeResponse.getStatusCodeValue)
      verifyRecipe(recipe, jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])

      verifyUrlsMatch(getRecipe,
        JsonPath.read(responseJson, "$.links[*].href").asInstanceOf[JSONArray].get(0).asInstanceOf[String])

      val updateRecipe = JsonPath.read(responseJson, "$.actions[?(@.method == 'PATCH')].href")
        .asInstanceOf[JSONArray].get(0).asInstanceOf[String]
      val updatedRecipe = Recipe.create("Another test", "Another test recipe",
        util.Arrays.asList(Ingredient.create("something else", QuantitySpecifier.Unspecified, 0.0)),
        util.Arrays.asList(Instruction.create("Do something else")))
      updatedRecipe.recipeId = recipeId.longValue
      updatedRecipe.ingredients.get(0).ingredientId =
        JsonPath.read(addRecipeJson, "$.ingredients[0].ingredientId").asInstanceOf[Integer].longValue
      updatedRecipe.instructions.get(0).instructionId =
        JsonPath.read(addRecipeJson, "$.instructions[0].instructionId").asInstanceOf[Integer].longValue

      val updateRecipeResponse =
        restTemplate.exchange(RequestEntity.patch(updateRecipe).accept(MediaType.APPLICATION_JSON).body(updatedRecipe),
          classOf[String])

      assertEquals(200, updateRecipeResponse.getStatusCodeValue)

      val updatedRecipeJson =
        jsonPath.parse(updateRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]

      verifyRecipe(updatedRecipe, updatedRecipeJson)

      deleteRecipe =
        JsonPath.read(responseJson, "$.actions[?(@.method == 'DELETE')].href")
          .asInstanceOf[JSONArray].get(0).asInstanceOf[String]
    } finally {
      if (deleteRecipe != null) {
        val deleteRecipeResponse = restTemplate.exchange(RequestEntity.delete(deleteRecipe).build, classOf[String])

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }

    verifyListRecipesSize(0)
  }

  @Test
  def testGetRecipeWithoutHyperlinks(): Unit = {
    var recipeId: Integer = null

    try {
      verifyListRecipesSize(0)

      val addRecipeResponse =
        restTemplate.exchange(RequestEntity
          .put(s"http://localhost:$port/recipes/add")
          .accept(MediaType.APPLICATION_JSON)
          .body(recipe), classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)

      val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
      recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

      val getRecipe = s"http://localhost:$port/recipes/get/$recipeId"
      val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])

      assertEquals(200, getRecipeResponse.getStatusCodeValue)
      verifyRecipe(recipe, jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])
    } finally {
      if (recipeId != null) {
        val deleteRecipeResponse = recipeController.deleteRecipe(recipeId.longValue())

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }

    verifyListRecipesSize(0)
  }


  @Test
  def testGetRecipeWithHyperlinks(): Unit = {
    var recipeId: Integer = null

    try {
      verifyListRecipesSize(0)

      val addRecipeResponse =
        restTemplate.exchange(RequestEntity
          .put(s"http://localhost:$port/recipes/add")
          .accept(MediaType.APPLICATION_JSON)
          .body(recipe), classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)

      val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
      recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

      val getRecipe = new URIBuilder(s"http://localhost:$port/recipes/get/$recipeId")
        .addParameter(includeHyperLinksParam, "true").build.toString
      val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])
      val responseJson = jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
      val getRecipeJson =
        JsonPath.read(responseJson, "$.properties").asInstanceOf[util.LinkedHashMap[String, Object]]

      assertEquals(200, getRecipeResponse.getStatusCodeValue)
      verifyRecipe(recipe, getRecipeJson)

      verifyUrlsMatch(getRecipe,
        JsonPath.read(responseJson, "$.links[*].href").asInstanceOf[JSONArray].get(0).asInstanceOf[String])

      val updatedRecipe = Recipe.create("Another test", "Another test recipe",
        util.Arrays.asList(Ingredient.create("something else", QuantitySpecifier.Unspecified, 0.0)),
        util.Arrays.asList(Instruction.create("Do something else")))
      updatedRecipe.recipeId = recipeId.longValue
      updatedRecipe.ingredients.get(0).ingredientId =
        JsonPath.read(getRecipeJson, "$.ingredients[0].ingredientId").asInstanceOf[Integer].longValue
      updatedRecipe.instructions.get(0).instructionId =
        JsonPath.read(getRecipeJson, "$.instructions[0].instructionId").asInstanceOf[Integer].longValue

      val updateRecipe = JsonPath.read(responseJson, "$.actions[?(@.method == 'PATCH')].href")
        .asInstanceOf[JSONArray].get(0).asInstanceOf[String]
      val updateRecipeResponse =
        restTemplate.exchange(RequestEntity.patch(updateRecipe)
          .accept(MediaType.APPLICATION_JSON)
          .body(updatedRecipe), classOf[String])

      assertEquals(200, updateRecipeResponse.getStatusCodeValue)

      val updatedRecipeJson =
        jsonPath.parse(updateRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]

      verifyRecipe(updatedRecipe, updatedRecipeJson)
    } finally {
      if (recipeId != null) {
        val deleteRecipeResponse = recipeController.deleteRecipe(recipeId.longValue())

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }

    verifyListRecipesSize(0)
  }

  @Test
  def testUpdateRecipeWithoutHyperlinks(): Unit = {
    var recipeId: Integer = null

    try {
      val updateRecipe = s"http://localhost:$port/recipes/update"

      verifyListRecipesSize(0)

      val addRecipeResponse =
        restTemplate.exchange(RequestEntity
          .put(s"http://localhost:$port/recipes/add")
          .accept(MediaType.APPLICATION_JSON)
          .body(recipe), classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)

      val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
      recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

      val updatedRecipe = Recipe.create("Another test", "Another test recipe",
        util.Arrays.asList(Ingredient.create("something else", QuantitySpecifier.Unspecified, 0.0)),
        util.Arrays.asList(Instruction.create("Do something else")))
      updatedRecipe.recipeId = recipeId.longValue
      updatedRecipe.ingredients.get(0).ingredientId =
        JsonPath.read(addRecipeJson, "$.ingredients[0].ingredientId").asInstanceOf[Integer].longValue
      updatedRecipe.instructions.get(0).instructionId =
        JsonPath.read(addRecipeJson, "$.instructions[0].instructionId").asInstanceOf[Integer].longValue

      val updateRecipeResponse = restTemplate.exchange(RequestEntity
        .patch(updateRecipe)
        .accept(MediaType.APPLICATION_JSON)
        .body(updatedRecipe), classOf[String])

      assertEquals(200, updateRecipeResponse.getStatusCodeValue)
      verifyRecipe(updatedRecipe,
        jsonPath.parse(updateRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])

      val getRecipe = s"http://localhost:$port/recipes/get/$recipeId"
      val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])

      assertEquals(200, getRecipeResponse.getStatusCodeValue)
      verifyRecipe(updatedRecipe,
        jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])
    } finally {
      if (recipeId != null) {
        val deleteRecipeResponse = recipeController.deleteRecipe(recipeId.longValue())

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }

    verifyListRecipesSize(0)
  }

  @Test
  def testUpdateRecipeWithHyperlinks(): Unit = {
    var deleteRecipe: String = null

    try {
      val updateRecipe = new URIBuilder(s"http://localhost:$port/recipes/update")
        .addParameter(includeHyperLinksParam, "true").build.toString

      verifyListRecipesSize(0)

      val addRecipeResponse = restTemplate.exchange(RequestEntity
        .put(s"http://localhost:$port/recipes/add")
        .accept(MediaType.APPLICATION_JSON)
        .body(recipe), classOf[String])

      assertEquals(200, addRecipeResponse.getStatusCodeValue)

      val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
      val recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

      val updatedRecipe = Recipe.create("Another test", "Another test recipe",
        util.Arrays.asList(Ingredient.create("something else", QuantitySpecifier.Unspecified, 0.0)),
        util.Arrays.asList(Instruction.create("Do something else")))
      updatedRecipe.recipeId = recipeId.longValue
      updatedRecipe.ingredients.get(0).ingredientId =
        JsonPath.read(addRecipeJson, "$.ingredients[0].ingredientId").asInstanceOf[Integer].longValue
      updatedRecipe.instructions.get(0).instructionId =
        JsonPath.read(addRecipeJson, "$.instructions[0].instructionId").asInstanceOf[Integer].longValue

      val updateRecipeResponse = restTemplate.exchange(RequestEntity
        .patch(updateRecipe)
        .accept(MediaType.APPLICATION_JSON)
        .body(updatedRecipe), classOf[String])

      assertEquals(200, updateRecipeResponse.getStatusCodeValue)

      val responseJson = jsonPath.parse(updateRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
      val updateRecipeJson =
        JsonPath.read(responseJson, "$.properties").asInstanceOf[util.LinkedHashMap[String, Object]]
      verifyRecipe(updatedRecipe, updateRecipeJson)

      val getRecipe = s"http://localhost:$port/recipes/get/$recipeId"
      val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])

      assertEquals(200, getRecipeResponse.getStatusCodeValue)
      verifyRecipe(updatedRecipe,
        jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])
      verifyUrlsMatch(getRecipe,
        JsonPath.read(responseJson, "$.links[*].href").asInstanceOf[JSONArray].get(0).asInstanceOf[String])

      val updatedRecipe2 = Recipe.create("Yet another test", "Yet another test recipe",
        util.Arrays.asList(Ingredient.create("something else again", QuantitySpecifier.Unspecified, 0.0)),
        util.Arrays.asList(Instruction.create("Do something else again")))
      updatedRecipe2.recipeId = recipeId.longValue
      updatedRecipe2.ingredients.get(0).ingredientId =
        JsonPath.read(updateRecipeJson, "$.ingredients[0].ingredientId").asInstanceOf[Integer].longValue
      updatedRecipe2.instructions.get(0).instructionId =
        JsonPath.read(updateRecipeJson, "$.instructions[0].instructionId").asInstanceOf[Integer].longValue

      val updateRecipe2 =
        JsonPath.read(responseJson, "$.actions[?(@.method == 'PATCH')].href")
          .asInstanceOf[JSONArray].get(0).asInstanceOf[String]
      val updateRecipeResponse2 = restTemplate.exchange(RequestEntity
        .patch(updateRecipe2)
        .accept(MediaType.APPLICATION_JSON)
        .body(updatedRecipe2), classOf[String])

      assertEquals(200, updateRecipeResponse2.getStatusCodeValue)

      val updatedRecipeJson2 =
        jsonPath.parse(updateRecipeResponse2.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]

      verifyRecipe(updatedRecipe2, updatedRecipeJson2)
      deleteRecipe = JsonPath.read(responseJson, "$.actions[?(@.method == 'DELETE')].href")
        .asInstanceOf[JSONArray].get(0).asInstanceOf[String]
    } finally {
      if (deleteRecipe != null) {
        val deleteRecipeResponse = restTemplate.exchange(RequestEntity.delete(deleteRecipe).build, classOf[String])

        assertEquals(200, deleteRecipeResponse.getStatusCodeValue)
      }
    }
    verifyListRecipesSize(0)
  }

  private def verifyListRecipesSize(expectedNumberOfElements: Int): Unit = {
    val listRecipes = s"http://localhost:$port/recipes/list"

    // Check for list recipes without hyper-links
    val listRecipesWithoutHyperLinksResponse =
      restTemplate.exchange(RequestEntity.get(listRecipes).build, classOf[String])

    assertEquals(200, listRecipesWithoutHyperLinksResponse.getStatusCodeValue)
    assertEquals(expectedNumberOfElements, new org.json.JSONArray(listRecipesWithoutHyperLinksResponse.getBody).length)

    // Check for list recipes with hyper-links
    val listRecipesWithHyperLinksResponse =
      restTemplate.exchange(RequestEntity
        .get(new URIBuilder(listRecipes)
          .addParameter(includeHyperLinksParam, "true")
          .build.toString).build, classOf[String])

    assertEquals(200, listRecipesWithHyperLinksResponse.getStatusCodeValue)
    val listRecipesWithHyperLinksJson = jsonPath.parse(listRecipesWithHyperLinksResponse.getBody)
    if (expectedNumberOfElements > 0) {
      assertEquals(expectedNumberOfElements, JsonPath.read(listRecipesWithHyperLinksJson,
        "$.properties.totalElements").asInstanceOf[Integer])
      assertEquals(expectedNumberOfElements,
        JsonPath.read(listRecipesWithHyperLinksJson,"$.entities").asInstanceOf[JSONArray].size)
    } else {
      assertEquals(expectedNumberOfElements, JsonPath.read(listRecipesWithHyperLinksJson,
        "$.properties.totalElements").asInstanceOf[Integer])
    }
  }

  private def verifyUrlsMatch(urlStr1: String, urlStr2: String): Unit = {
    val url1 = new URL(urlStr1)
    val url2 = new URL(urlStr2)

    assertEquals(url1.getProtocol, url2.getProtocol)
    assertEquals(url1.getHost, url2.getHost)
    assertEquals(url1.getPort, url2.getPort)
    assertEquals(url1.getPath, url2.getPath)
  }

  private def verifyRecipe(expectedRecipe: Recipe, actualRecipeJson: util.LinkedHashMap[String, Object]): Unit = {

    assertEquals(expectedRecipe.name, JsonPath.read(actualRecipeJson, "$.name").asInstanceOf[String])
    assertEquals(expectedRecipe.description,
      JsonPath.read(actualRecipeJson, "$.description").asInstanceOf[String])

    val getIngredientsJson = JsonPath.read(actualRecipeJson, "$.ingredients").asInstanceOf[JSONArray]

    assertEquals(expectedRecipe.ingredients.size, getIngredientsJson.size)
    for (i <- 0 until expectedRecipe.ingredients.size) {
      assertEquals(expectedRecipe.ingredients.get(i).quantitySpecifier.toString,
        JsonPath.read(getIngredientsJson, "$[%d].quantitySpecifier".format(i)).asInstanceOf[String])
      assertEquals(expectedRecipe.ingredients.get(i).quantity,
        JsonPath.read(getIngredientsJson, "$[%d].quantity".format(i)).asInstanceOf[Double])
      assertEquals(expectedRecipe.ingredients.get(i).ingredient,
        JsonPath.read(getIngredientsJson, "$[%d].ingredient".format(i)).asInstanceOf[String])
    }

    val getInstructionsJson = JsonPath.read(actualRecipeJson, "$.instructions").asInstanceOf[JSONArray]

    assertEquals(expectedRecipe.instructions.size, getInstructionsJson.size)
    for (i <- 0 until expectedRecipe.instructions.size) {
      assertEquals(expectedRecipe.instructions.get(i).instruction,
        JsonPath.read(getInstructionsJson, "$[%d].instruction".format(i)).asInstanceOf[String])
    }
  }

  @throws[URISyntaxException]
  private def getResourcePath = {
    var classResourceUri = this.getClass.getResource("").toURI
    for (i <- 0 until this.getClass.toString.split("\\.").length - 1) {
      classResourceUri = Paths.get(classResourceUri).getParent.toUri
    }
    classResourceUri.getPath
  }

  @throws[IOException]
  @throws[URISyntaxException]
  private def writeConfigurationYmlFile(configurationMapping: util.Map[String, _]): Unit = {
    val resource = getClass.getResource("/application-test.yml.ftl")
    val source = scala.io.Source.fromFile(resource.getPath)
    val templateString = try source.mkString finally source.close()
    val substitutor = new StringSubstitutor(configurationMapping)
    val resolvedString = substitutor.replace(templateString)
    Files.write(Paths.get(getResourcePath, CONFIGURATION_YML_FILE), resolvedString.getBytes)
  }

  @throws[CertificateException]
  @throws[IOException]
  private def addElasticsearchCertificate(certificates: ListBuffer[Pair[String, Certificate]], container: ElasticsearchContainer): Unit = {
    val caCerts: Optional[Array[Byte]] = container.caCertAsBytes
    if (caCerts.isPresent) {
      val fis: InputStream = new ByteArrayInputStream(caCerts.get)
      val bis: BufferedInputStream = new BufferedInputStream(fis)
      val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
      while (bis.available > 0) {
        val cert: Certificate = cf.generateCertificate(bis)
        val alias: String = "Elasticsearch" + bis.available
        certificates +=new Pair(alias, cert)
      }
    }
  }

  @throws[KeyStoreException]
  @throws[IOException]
  @throws[NoSuchAlgorithmException]
  @throws[CertificateException]
  @throws[URISyntaxException]
  private def createKeyStore(certificates: List[Pair[String, Certificate]]): Unit = {
    val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType)
    keyStore.load(null) //Make an empty store

    val keyStoreFile: File = Paths.get(getResourcePath.toString, "keystore.jks").toFile
    for (certificate <- certificates) {
      keyStore.setCertificateEntry(certificate.getKey, certificate.getValue)
    }

    val stream: FileOutputStream = new FileOutputStream(keyStoreFile)
    try keyStore.store(stream, KEYSTORE_PASSWORD.toCharArray)
    finally {
      if (stream != null) stream.close()
    }

    System.setProperty("javax.net.ssl.trustStore", keyStoreFile.getAbsolutePath)
    System.setProperty("javax.net.ssl.trustStorePassword", KEYSTORE_PASSWORD)
  }
}
