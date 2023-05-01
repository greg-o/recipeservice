package org.grego.recipeservice.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ShardStatistics
import co.elastic.clients.elasticsearch.core.{SearchRequest, SearchResponse}
import co.elastic.clients.elasticsearch.core.search.HitsMetadata
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.grego.recipeservice.document.RecipeDoc
import org.grego.recipeservice.model.{Ingredient, Instruction, Recipe}
import org.grego.recipeservice.repository.{RecipeRepository, RecipeSearchRepository}
import org.instancio.Instancio
import org.jetbrains.annotations.NotNull
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertThrows, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.mockito.{InjectMocks, Mock}
import org.mockito.junit.jupiter.MockitoExtension
import org.powermock.modules.junit4.PowerMockRunner

import java.util.{Collections, HashMap, List, Map, Optional}
import java.util.stream.Collectors
import org.mockito.ArgumentMatchers.{any, anyInt, anyLong, anyString}
import org.mockito.Mockito.{atLeastOnce, eq, times, verify, verifyNoMoreInteractions, when}
import org.springframework.data.domain.{Page, PageImpl, PageRequest}
import org.springframework.data.util.Streamable
import org.springframework.web.server.ResponseStatusException

import java.util

/**
 * Tests for the RecipeService using mock ReactiveElasticsearchOperations,
 * ReactiveElasticsearchClient, RecipeRepository, IngredientRepository,
 * InstructionRepository, RecipeSearchRepository, and DatabaseClient.
 */
@ExtendWith(Array(classOf[MockitoExtension]))
@RunWith(classOf[PowerMockRunner])
class RecipeServiceTest {

  /**
   * Count of zero.
   */
  val COUNT_ZERO = 0
  /**
   * First page.
   */
  private val PAGE_NUMBER_1 = 1

  /**
   * Page size of 10.
   */
  private val PAGE_SIZE_10 = 10

  /**
   * Recipe id for a recipe that doesn't exist.
   */
  val RECIPE_ID = 0

  /**
   * The value returned for update.
   */
  val LONG_RETURN_VALUE = 0L

  /**
   * Search text to search Elasticsearch.
   */
  val SEARCH_TEXT = "search text"

  /**
   * Elasticsearch took.
   */
  val TOOK_ELASTICSEARCH = 3

  /**
   * Ingredient field name mapping.
   */
  private val INGREDIENT_FIELD_NAME_MAPPING = util.Map.of(
    "ingredientId", "ingredient_id",
    "quantity", "quantity",
    "ingredient", "ingredient",
    "ingredientNumber", "ingredient_number",
    "quantitySpecifier", "quantity_specifier"
  )

  /**
   * Instruction field name mapping.
   */
  private val INSTRUCTION_FIELD_NAME_MAPPING = util.Map.of(
    "instructionId", "instruction_id",
    "instructionNumber", "instruction_number",
    "instruction", "instruction"
  )

  /**
   * Instance of the RecipeService that is being tested.
   */
  @InjectMocks
  private var recipeService: RecipeService = _


  @Mock
  private var recipeRepository: RecipeRepository = _

  @Mock
  private var recipeSearchRepository: RecipeSearchRepository = _

  @Mock
  private var elasticsearchClient: ElasticsearchClient = _

  /**
   * To convert objects to JSON and JSON to maps.
   */
  private val objectMapper: ObjectMapper = new ObjectMapper

  /**
   * Test getAllRecipes with no recipes.
   */
  @Test
  def testGetAllRecipesEmpty: Unit = {
    val page = Page.empty

    when(recipeRepository.findAll(any(classOf[PageRequest]))).thenReturn(page)

    val response = recipeService.getAllRecipes(PAGE_NUMBER_1, PAGE_SIZE_10)

    assertEquals(0, response.getSize)
    assertEquals(0, response.getTotalElements)
    assertEquals(1, response.getTotalPages)

    verify(recipeRepository, times(1)).findAll(any(classOf[PageRequest]))
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test getAllRecipes with one recipe.
   */
  @Test
  def testGetAllRecipesOneRecipe: Unit = {
    val recipes = Instancio.ofList(classOf[Recipe]).size(1).create
    val page = new PageImpl[Recipe](recipes, PageRequest.of(0, PAGE_SIZE_10), recipes.size)

    when(recipeRepository.findAll(any(classOf[PageRequest]))).thenReturn(page)

    val response = recipeService.getAllRecipes(PAGE_NUMBER_1, PAGE_SIZE_10)

    assertEquals(PAGE_SIZE_10, response.getSize)
    assertEquals(recipes.size, response.getTotalElements)
    assertEquals(1, response.getTotalPages)

    verify(recipeRepository, times(1)).findAll(any(classOf[PageRequest]))
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test getAllRecipes with one recipe.
   */
  @Test
  def testGetAllRecipesMultiRecipe: Unit = {
    val recipes = Instancio.ofList(classOf[Recipe]).size(10).create
    val totalSize = 100
    val page = new PageImpl[Recipe](recipes, PageRequest.of(0, PAGE_SIZE_10), totalSize)

    when(recipeRepository.findAll(any(classOf[PageRequest]))).thenReturn(page)

    val response = recipeService.getAllRecipes(PAGE_NUMBER_1, PAGE_SIZE_10)

    assertEquals(PAGE_SIZE_10, response.getSize)
    assertEquals(totalSize, response.getTotalElements)
    assertEquals(totalSize / PAGE_SIZE_10, response.getTotalPages)

    verify(recipeRepository, times(1)).findAll(any(classOf[PageRequest]))
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test recipeExistsById where recipe does not exist.
   */
  @Test
  def testRecipesExistsByIdDoesNotExist: Unit = {

    when(recipeRepository.existsById(anyLong)).thenReturn(false)

    val response = recipeService.recipeExistsById(RECIPE_ID)

    assertFalse(response)

    verify(recipeRepository, times(1)).existsById(anyLong)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test recipeExistsById where recipe exists.
   */
  @Test
  def testRecipesExistsByIdExists: Unit = {

    when(recipeRepository.existsById(anyLong)).thenReturn(true)

    val response = recipeService.recipeExistsById(RECIPE_ID)

    assertTrue(response)

    verify(recipeRepository, times(1)).existsById(anyLong)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test getRecipeById where recipe does not exist.
   */
  @Test
  def testGetRecipesByIdDoesNotExist: Unit = {

    when(recipeRepository.findById(anyLong)).thenReturn(Optional.empty())

    val response = recipeService.getRecipeById(RECIPE_ID)

    assertTrue(response.isEmpty)

    verify(recipeRepository, times(1)).findById(anyLong)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test getRecipeById where recipe exists.
   */
  @Test
  def testGetRecipesByIdExists: Unit = {

    val recipe = Instancio.create(classOf[Recipe])

    when(recipeRepository.findById(anyLong)).thenReturn(Optional.of(recipe))

    val response = recipeService.getRecipeById(RECIPE_ID)

    assertTrue(response.isPresent)
    assertEquals(recipe, response.get)

    verify(recipeRepository, times(1)).findById(anyLong)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test addRecipe where recipes with the same name do not exist.
   */
  @Test
  def testAddRecipeSameNameDoesNotExist: Unit = {
    val recipe = Instancio.create(classOf[Recipe])
    val existingRecipes: Streamable[Recipe] = Streamable.empty

    when(recipeRepository.findAllByName(anyString)).thenReturn(existingRecipes)
    when(recipeRepository.save(any(classOf[Recipe]))).thenReturn(recipe)

    val response = recipeService.addRecipe(recipe)

    verify(recipeRepository, times(1)).findAllByName(anyString)
    verify(recipeRepository, times(1)).save(any(classOf[Recipe]))
    verify(recipeSearchRepository, times(1)).save(any(classOf[RecipeDoc]))
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test addRecipe where recipes with the same name exists.
   */
  @Test
  def testAddRecipeSameNameExists: Unit = {
    val recipe = Instancio.create(classOf[Recipe])
    val existingRecipes: Streamable[Recipe] = Streamable.of(Instancio.ofList(classOf[Recipe]).size(2).create)

    when(recipeRepository.findAllByName(anyString)).thenReturn(existingRecipes)
    when(recipeRepository.save(any(classOf[Recipe]))).thenReturn(recipe)

    val response = recipeService.addRecipe(recipe)

    verify(recipeRepository, times(1)).findAllByName(anyString)
    verify(recipeRepository, times(1)).save(any(classOf[Recipe]))
    verify(recipeSearchRepository, times(1)).save(any(classOf[RecipeDoc]))
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test updateRecipe where the recipe does not exist.
   */
  @Test
  def testUpdateRecipeDoesNotExist: Unit = {
    val recipe = Instancio.create(classOf[Recipe])

    when(recipeRepository.findById(anyLong)).thenReturn(Optional.empty)

    assertThrows(classOf[ResponseStatusException], () => recipeService.updateRecipe(recipe))

    verify(recipeRepository, times(1)).findById(anyLong)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test updateRecipe where the recipe exists.
   */
  @Test
  def testUpdateRecipeExists: Unit = {
    val recipe = Instancio.create(classOf[Recipe])

    when(recipeRepository.findById(anyLong)).thenReturn(Optional.of(recipe))
    when(recipeRepository.save(any(classOf[Recipe]))).thenReturn(recipe)

    recipeService.updateRecipe(recipe)

    verify(recipeRepository, times(1)).findById(anyLong)
    verify(recipeRepository, times(1)).save(any(classOf[Recipe]))
    verify(recipeSearchRepository, times(1)).save(any(classOf[RecipeDoc]))
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test deleteRecipeById.
   */
  @Test
  def testDeleteRecipesByIdDoesNotExist: Unit = {

    recipeService.deleteRecipeById(RECIPE_ID)

    verify(recipeRepository, times(1)).deleteById(anyLong)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }

  /**
   * Test searchRecipes.
   */
  @Test
  def testSearchRecipes: Unit = {
    val searchResponse: SearchResponse[RecipeDoc] = new SearchResponse.Builder[RecipeDoc]()
      .shards(new ShardStatistics.Builder().successful(0).failed(0).total(0).build)
      .took(TOOK_ELASTICSEARCH)
      .timedOut(false)
      .hits(new HitsMetadata.Builder[RecipeDoc]().hits(Collections.emptyList).build)
      .build


    when(elasticsearchClient.search(any(classOf[SearchRequest]), any)).thenReturn(searchResponse)

    val response = recipeService.searchRecipes("Recipe")

    assertEquals(0, response.hits.hits.size)

    verify(elasticsearchClient, times(1)).search(any(classOf[SearchRequest]), any)
    verifyNoMoreInteractions(elasticsearchClient, recipeRepository, recipeSearchRepository)
  }
}