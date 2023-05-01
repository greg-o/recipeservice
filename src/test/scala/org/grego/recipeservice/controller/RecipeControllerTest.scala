package org.grego.recipeservice.controller


import co.elastic.clients.elasticsearch._types.ShardStatistics
import co.elastic.clients.elasticsearch.core.search.{HitsMetadata, ResponseBody}
import co.elastic.clients.elasticsearch.core.SearchResponse
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.jayway.jsonpath.{Configuration, JsonPath}
import com.jayway.jsonpath.spi.json.JsonProvider
import net.minidev.json.JSONArray
import org.grego.recipeservice.document.RecipeDoc
import org.grego.recipeservice.model.Recipe
import org.grego.recipeservice.service.IRecipeService
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.{assertEquals, assertThrows}
import org.junit.jupiter.api.{BeforeAll, Tag, Test, TestInstance}
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.mockito.{InjectMocks, Mock, Spy}
import org.mockito.junit.jupiter.MockitoExtension
import org.powermock.modules.junit4.PowerMockRunner
import org.springframework.http.{HttpStatus, MediaType}
import org.springframework.http.ResponseEntity

import java.util.{Collections, List, Optional, Random}
import org.mockito.ArgumentMatchers.{any, anyBoolean, anyInt, anyLong, anyString}
import org.mockito.Mockito.{doThrow, times, verify, verifyNoInteractions, verifyNoMoreInteractions, when}
import org.mockito.invocation.InvocationOnMock
import org.springframework.data.domain.{Page, PageImpl, PageRequest}
import org.springframework.web.server.ResponseStatusException

import java.util

/**
 * Test RecipeController with mock IRecipeService and RecipeResourceAssembler.
 */
@ExtendWith(Array(classOf[MockitoExtension]))
@RunWith(classOf[PowerMockRunner])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("UnitTests")
class RecipeControllerTest {

  /**
   * Invalid page number (pages start at 1).
   */
  val INVALID_PAGE_NUMBER = -1L

  /**
   * First page.
   */
  private val PAGE_NUMBER_1 = 1L

  /**
   * Page size of 10.
   */
  private val PAGE_SIZE_10 = 10

  /**
   * Include hyper-links.
   */
  private val INCLUDE_HYPER_LINKS = true

  /**
   * Don't include hyper-links.
   */
  private val DO_NOT_INCLUDE_HYPER_LINKS = false

  /**
   * The time it took Elasticsearch.
   */
  val TOOK_ELASTICSEARCH = 3L
  /**
   * Instance RecipeController to test against.
   */
  @InjectMocks private var recipeController: RecipeController = _

  /**
   * Mock IRecipeService.
   */
  @Mock private var recipeService: IRecipeService = _

  /**
   * Mock RecipeResourceAssembler.
   */
  @Mock private var recipeResourceAssembler: RecipeResourceAssembler = _

  /**
   * Instance of objectMapper.
   */
  @Spy private val objectMapper = new ObjectMapper

  /**
   * JsonProvider is used to execute JSON path queries.
   */
  private val jsonPath = Configuration.defaultConfiguration.jsonProvider

  /**
   * Before running tests set up objectMapper.
   */
  @BeforeAll private[controller] def init(): Unit = {
    objectMapper.registerModule(new JavaTimeModule)
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }

  /**
   * Test list recipes with hyper-links and an invalid page number.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithHyperLinksInvalidPage: Unit = {
    val response = recipeController.listRecipes(Optional.of(INVALID_PAGE_NUMBER.toString),
      Optional.of(PAGE_SIZE_10.toString), INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode)
    assertEquals(MediaType.TEXT_PLAIN_VALUE, response.getHeaders.getContentType.toString)

    verifyNoInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test list recipes without hyper-links and an invalid page number.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithoutHyperLinksInvalidPage: Unit = {
    val response = recipeController.listRecipes(Optional.of(INVALID_PAGE_NUMBER.toString),
      Optional.of(PAGE_SIZE_10.toString), DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode)
    assertEquals(MediaType.TEXT_PLAIN_VALUE, response.getHeaders.getContentType.toString)

    verifyNoInteractions(recipeService, recipeResourceAssembler)
  }


  /**
   * Test list recipes with hyper-links and no recipes.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithHyperLinksEmpty: Unit = {
    val assembler = new RecipeResourceAssembler

    when(recipeService.getAllRecipes(any, any)).thenReturn(Page.empty())
    when(recipeResourceAssembler.toCollectionModel(any))
      .thenAnswer((invocation: InvocationOnMock) => assembler.toCollectionModel(invocation.getArgument(0)))

    val response = recipeController.listRecipes(Optional.of(PAGE_NUMBER_1.toString),
      Optional.of(PAGE_SIZE_10.toString), INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getAllRecipes(any, any)
    verify(recipeResourceAssembler, times(1)).toCollectionModel(any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test list recipes without hyper-links and no recipes.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithoutHyperLinksEmpty: Unit = {
    when(recipeService.getAllRecipes(any, any)).thenReturn(Page.empty())

    val response = recipeController.listRecipes(Optional.of(PAGE_NUMBER_1.toString),
      Optional.of(PAGE_SIZE_10.toString), DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getAllRecipes(any, any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test list recipes with hyper-links and one recipe.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithHyperLinksWithOneRecipe: Unit = {
    val recipes = Instancio.ofList(classOf[Recipe]).size(1).create
    val page = new PageImpl[Recipe](recipes, PageRequest.of(1, PAGE_SIZE_10), recipes.size)
    val assembler = new RecipeResourceAssembler

    when(recipeService.getAllRecipes(any, any)).thenReturn(page)
    when(recipeResourceAssembler.toCollectionModel(any))
      .thenAnswer((invocation: InvocationOnMock) => assembler.toCollectionModel(invocation.getArgument(0)))

    val response = recipeController.listRecipes(Optional.of(PAGE_NUMBER_1.toString),
      Optional.of(PAGE_SIZE_10.toString), INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getAllRecipes(any, any)
    verify(recipeResourceAssembler, times(1)).toCollectionModel(any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test list recipes without hyper-links and one recipe.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithoutHyperLinksWithOneRecipe: Unit = {
    val recipes = Instancio.ofList(classOf[Recipe]).size(1).create
    val page = new PageImpl[Recipe](recipes, PageRequest.of(1, PAGE_SIZE_10), recipes.size)

    when(recipeService.getAllRecipes(any, any)).thenReturn(page)

    val response = recipeController.listRecipes(Optional.of(PAGE_NUMBER_1.toString),
      Optional.of(PAGE_SIZE_10.toString), DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getAllRecipes(any, any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test list recipes with hyper-links and more than one recipe.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithHyperLinksWithMoreThanOneRecipe: Unit = {
    val recipes = Instancio.ofList(classOf[Recipe]).size(new Random().nextInt(PAGE_SIZE_10 - 2) + 2).create
    val page = new PageImpl[Recipe](recipes, PageRequest.of(1, PAGE_SIZE_10), recipes.size)
    val assembler = new RecipeResourceAssembler

    when(recipeService.getAllRecipes(any, any)).thenReturn(page)
    when(recipeResourceAssembler.toCollectionModel(any))
      .thenAnswer((invocation: InvocationOnMock) => assembler.toCollectionModel(invocation.getArgument(0)))

    val response = recipeController.listRecipes(Optional.of(PAGE_NUMBER_1.toString),
      Optional.of(PAGE_SIZE_10.toString), INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getAllRecipes(any, any)
    verify(recipeResourceAssembler, times(1)).toCollectionModel(any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test list recipes without hyper-links and more than one recipe.
   *
   * @throws Exception
   */
  @Test
  def testListRecipesWithoutHyperLinksWithMoreThanOneRecipe: Unit = {
    val recipes = Instancio.ofList(classOf[Recipe]).size(new Random().nextInt(PAGE_SIZE_10 - 2) + 2).create
    val page = new PageImpl[Recipe](recipes, PageRequest.of(1, PAGE_SIZE_10), recipes.size)

    when(recipeService.getAllRecipes(any, any)).thenReturn(page)

    val response = recipeController.listRecipes(Optional.of(PAGE_NUMBER_1.toString),
      Optional.of(PAGE_SIZE_10.toString), DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getAllRecipes(any, any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test get recipe with hyper-links where recipe doesn't exist.
   *
   * @throws Exception
   */
  @Test
  def testGetRecipeWithHyperLinksDoesNotExist(): Unit = {
    when(recipeService.getRecipeById(anyLong)).thenReturn(Optional.empty)

    val response = recipeController.getRecipe(-1L, INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode)

    verify(recipeService, times(1)).getRecipeById(anyLong)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test get recipe without hyper-links where recipe doesn't exist.
   *
   * @throws Exception
   */
  @Test
  def testGetRecipeWithoutHyperLinksDoesNotExist: Unit = {
    when(recipeService.getRecipeById(anyLong)).thenReturn(Optional.empty)

    val response = recipeController.getRecipe(-1L, DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode)

    verify(recipeService, times(1)).getRecipeById(anyLong)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test get recipe with hyper-links where recipe exists.
   *
   * @throws Exception
   */
  @Test
  def testGetRecipeWithHyperLinksExists: Unit = {
    val recipe = Instancio.create(classOf[Recipe])
    val assembler = new RecipeResourceAssembler

    when(recipeService.getRecipeById(anyLong)).thenReturn(Optional.of(recipe))
    when(recipeResourceAssembler.toModel(any))
      .thenAnswer((invocation: InvocationOnMock) => assembler.toModel(invocation.getArgument(0)))

    val response = recipeController.getRecipe(-1L, INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getRecipeById(anyLong)
    verify(recipeResourceAssembler, times(1)).toModel(any)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test get recipe without hyper-links where recipe exists.
   *
   * @throws Exception
   */
  @Test
  def testGetRecipeWithoutHyperLinksExists: Unit = {
    val recipe = Instancio.create(classOf[Recipe])

    when(recipeService.getRecipeById(anyLong)).thenReturn(Optional.of(recipe))

    val response = recipeController.getRecipe(-1L, DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(MediaType.APPLICATION_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).getRecipeById(anyLong)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test add recipe with hyper-links.
   *
   * @throws Exception
   */
  @Test
  def testAddRecipeWithHyperLinks: Unit = {
    val recipe = Instancio.create(classOf[Recipe])
    val assembler = new RecipeResourceAssembler

    when(recipeService.addRecipe(any(classOf[Recipe]))).thenReturn(recipe)
    when(recipeResourceAssembler.toModel(any(classOf[Recipe])))
      .thenAnswer((invocation: InvocationOnMock) => assembler.toModel(invocation.getArgument(0)))

    val response = recipeController.addRecipe(recipe, INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).addRecipe(any(classOf[Recipe]))
    verify(recipeResourceAssembler, times(1)).toModel(any(classOf[Recipe]))
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test add recipe without hyper-links.
   *
   * @throws Exception
   */
  @Test
  def testAddRecipeWithoutHyperLinks: Unit = {
    val recipe = Instancio.create(classOf[Recipe])

    when(recipeService.addRecipe(any(classOf[Recipe]))).thenReturn(recipe)

    val response = recipeController.addRecipe(recipe, DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(MediaType.APPLICATION_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).addRecipe(any(classOf[Recipe]))
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test update recipe with hyper-links where recipe doesn't exist.
   *
   * @throws Exception
   */
  @Test
  def testUpdateRecipeWithHyperLinksDoesNotExist: Unit = {
    val recipe = Instancio.create(classOf[Recipe])

    when(recipeService.updateRecipe(any(classOf[Recipe])))
      .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))

    assertThrows(classOf[ResponseStatusException],
      () => recipeController.updateRecipe(recipe, INCLUDE_HYPER_LINKS))

    verify(recipeService, times(1)).updateRecipe(any(classOf[Recipe]))
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test update recipe without hyper-links where recipe doesn't exist.
   *
   * @throws Exception
   */
  @Test
  def testUpdateRecipeWithoutHyperLinksDoesNotExist: Unit = {
    val recipe = Instancio.create(classOf[Recipe])

    when(recipeService.updateRecipe(any(classOf[Recipe])))
      .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))

    assertThrows(classOf[ResponseStatusException],
      () => recipeController.updateRecipe(recipe, DO_NOT_INCLUDE_HYPER_LINKS))

    verify(recipeService, times(1)).updateRecipe(any(classOf[Recipe]))
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test update recipe with hyper-links where recipe exists.
   *
   * @throws Exception
   */
  @Test
  def testUpdateRecipeWithHyperLinksExists(): Unit = {
    val recipe = Instancio.create(classOf[Recipe])
    val assembler = new RecipeResourceAssembler

    when(recipeService.updateRecipe(any(classOf[Recipe]))).thenReturn(recipe)
    when(recipeResourceAssembler.toModel(any(classOf[Recipe])))
      .thenAnswer((invocation: InvocationOnMock) => assembler.toModel(invocation.getArgument(0)))

    val response = recipeController.updateRecipe(recipe, INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).updateRecipe(any(classOf[Recipe]))
    verify(recipeResourceAssembler, times(1)).toModel(any(classOf[Recipe]))
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test update recipe without hyper-links where recipe exists.
   *
   * @throws Exception
   */
  @Test
  def testUpdateRecipeWithoutHyperLinksExists(): Unit = {
    val recipe = Instancio.create(classOf[Recipe])
    val assembler = new RecipeResourceAssembler

    when(recipeService.updateRecipe(any(classOf[Recipe]))).thenReturn(recipe)

    val response = recipeController.updateRecipe(recipe, DO_NOT_INCLUDE_HYPER_LINKS)

    assertEquals(HttpStatus.OK, response.getStatusCode)
    assertEquals(MediaType.APPLICATION_JSON_VALUE,
      response.getHeaders.getContentType.toString)

    verify(recipeService, times(1)).updateRecipe(any(classOf[Recipe]))
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }


  /**
   * Test delete recipe where recipe doesn't exist.
   *
   * @throws Exception
   */
  @Test
  def testDeleteRecipeDoesNotExist: Unit = {
    when(recipeService.recipeExistsById(anyLong)).thenReturn(false)

    val response = recipeController.deleteRecipe(0L)

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode)

    verify(recipeService, times(1)).recipeExistsById(anyLong)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test delete recipe where recipe exists.
   *
   * @throws Exception
   */
  @Test
  def testDeleteRecipeExists(): Unit = {
    when(recipeService.recipeExistsById(anyLong)).thenReturn(true)

    val response = recipeController.deleteRecipe(0L)

    assertEquals(HttpStatus.OK, response.getStatusCode)

    verify(recipeService, times(1)).recipeExistsById(anyLong)
    verify(recipeService, times(1)).deleteRecipeById(anyLong)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }

  /**
   * Test delete recipe where recipe doesn't exist.
   *
   * @throws Exception
   */
  @Test
  def testSearchRecipesNoneFound: Unit = {
    val searchResponse: SearchResponse[RecipeDoc] = new SearchResponse.Builder[RecipeDoc]().shards(new ShardStatistics.Builder().successful(0).failed(0).total(0).build).took(TOOK_ELASTICSEARCH).timedOut(false).hits(new HitsMetadata.Builder[RecipeDoc]().hits(Collections.emptyList).build).build

    when(recipeService.searchRecipes(anyString)).thenReturn(searchResponse)

    val response: ResponseEntity[_] = recipeController.searchRecipes("search string")

    assertEquals(HttpStatus.OK, response.getStatusCode)

    verify(recipeService, times(1)).searchRecipes(anyString)
    verifyNoMoreInteractions(recipeService, recipeResourceAssembler)
  }
}
