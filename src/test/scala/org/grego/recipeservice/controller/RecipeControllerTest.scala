package org.grego.recipeservice.controller

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.jayway.jsonpath.{Configuration, JsonPath}
import net.minidev.json.JSONArray
import org.apache.http.client.utils
import org.apache.http.client.utils.URIBuilder
import org.grego.recipeservice.model.{Ingredient, Instruction, QuantitySpecifier, Recipe}
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{MediaType, RequestEntity, ResponseEntity}

import java.io.ByteArrayOutputStream
import java.net.URL
import java.util
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap

@SuppressWarnings(value = Array("scalastyle:magicnumber"))
class RecipeControllerTest extends BaseAppTest {
  @Autowired
  var recipeController: RecipeController = _

  private val jsonPath = Configuration.defaultConfiguration.jsonProvider
  private final val recipe = Recipe.create("Test", "Test recipe",
    util.Arrays.asList(Ingredient.create("something", QuantitySpecifier.Unspecified, 0.0)),
    util.Arrays.asList(Instruction.create("Do something")))
  private final val includeHyperLinksParam = "include-hyper-links"

  @Test
  def testListRecipes(): Unit = {
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
    val recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]
    assertNotNull(recipeId)

    recipeController.deleteRecipe(recipeId.longValue)

    verifyListRecipesSize(0)
  }

  @Test
  def testAddRecipeWithoutHyperlinks(): Unit = {
    val addRecipe = s"http://localhost:$port/recipes/add"

    verifyListRecipesSize(0)

    val addRecipeResponse =
      restTemplate.exchange(RequestEntity.put(addRecipe).accept(MediaType.APPLICATION_JSON).body(recipe),
        classOf[String])

    assertEquals(200, addRecipeResponse.getStatusCodeValue)

    val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]

    verifyRecipe(recipe, addRecipeJson)

    val recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

    assertNotNull(recipeId)

    val getRecipeResponse =
      restTemplate.exchange(RequestEntity.get(s"http://localhost:$port/recipes/get/$recipeId").build,
        classOf[String])

    assertEquals(200, getRecipeResponse.getStatusCodeValue)
    verifyRecipe(recipe, jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])

    val response = recipeController.deleteRecipe(recipeId.longValue())

    verifyListRecipesSize(0)
  }

  @Test
  def testAddRecipeWithHyperlinks(): Unit = {
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

    val deleteRecipe =
      JsonPath.read(responseJson, "$.actions[?(@.method == 'DELETE')].href")
        .asInstanceOf[JSONArray].get(0).asInstanceOf[String]

    val deleteRecipeResponse = restTemplate.exchange(RequestEntity.delete(deleteRecipe).build, classOf[String])

    assertEquals(200, deleteRecipeResponse.getStatusCodeValue)

    verifyListRecipesSize(0)
  }

  @Test
  def testGetRecipeWithoutHyperlinks(): Unit = {
    verifyListRecipesSize(0)

    val addRecipeResponse =
      restTemplate.exchange(RequestEntity
        .put(s"http://localhost:$port/recipes/add")
        .accept(MediaType.APPLICATION_JSON)
        .body(recipe), classOf[String])

    assertEquals(200, addRecipeResponse.getStatusCodeValue)

    val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
    val recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

    val getRecipe = s"http://localhost:$port/recipes/get/$recipeId"
    val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])

    assertEquals(200, getRecipeResponse.getStatusCodeValue)
    verifyRecipe(recipe, jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])

    val response = recipeController.deleteRecipe(recipeId.longValue())

    verifyListRecipesSize(0)
  }


  @Test
  def testGetRecipeWithHyperlinks(): Unit = {
    verifyListRecipesSize(0)

    val addRecipeResponse =
      restTemplate.exchange(RequestEntity
        .put(s"http://localhost:$port/recipes/add")
        .accept(MediaType.APPLICATION_JSON)
        .body(recipe), classOf[String])

    assertEquals(200, addRecipeResponse.getStatusCodeValue)

    val addRecipeJson = jsonPath.parse(addRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]]
    val recipeId = JsonPath.read(addRecipeJson, "$.recipeId").asInstanceOf[Integer]

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

    val response = recipeController.deleteRecipe(recipeId.longValue())

    verifyListRecipesSize(0)
  }

  @Test
  def testUpdateRecipeWithoutHyperlinks(): Unit = {
    val updateRecipe = s"http://localhost:$port/recipes/update"

    verifyListRecipesSize(0)

    val addRecipeResponse =
      restTemplate.exchange(RequestEntity
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
    verifyRecipe(updatedRecipe,
      jsonPath.parse(updateRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])

    val getRecipe = s"http://localhost:$port/recipes/get/$recipeId"
    val getRecipeResponse = restTemplate.exchange(RequestEntity.get(getRecipe).build, classOf[String])

    assertEquals(200, getRecipeResponse.getStatusCodeValue)
    verifyRecipe(updatedRecipe,
      jsonPath.parse(getRecipeResponse.getBody).asInstanceOf[util.LinkedHashMap[String, Object]])

    val response = recipeController.deleteRecipe(recipeId.longValue())

    verifyListRecipesSize(0)
  }

  @Test
  def testUpdateRecipeWithHyperlinks(): Unit = {
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

    val deleteRecipe = JsonPath.read(responseJson, "$.actions[?(@.method == 'DELETE')].href")
      .asInstanceOf[JSONArray].get(0).asInstanceOf[String]
    val deleteRecipeResponse = restTemplate.exchange(RequestEntity.delete(deleteRecipe).build, classOf[String])

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
    assertEquals(expectedNumberOfElements, JsonPath.read(listRecipesWithHyperLinksJson,
      "$.properties.totalElements").asInstanceOf[Integer])
    if (expectedNumberOfElements > 0)
      assertEquals(expectedNumberOfElements,
        JsonPath.read(listRecipesWithHyperLinksJson,"$.entities").asInstanceOf[JSONArray].size)
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
}
