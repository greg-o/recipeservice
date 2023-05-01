// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.service

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.{MatchQuery, Query, QueryStringQuery}
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.search.ResponseBody
import org.grego.recipeservice.controller.{RecipeController, RecipeResourceAssembler}
import org.grego.recipeservice.document.RecipeDoc
import org.grego.recipeservice.model.{Ingredient, Recipe}
import org.grego.recipeservice.repository.{RecipeRepository, RecipeSearchRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.data.jpa.repository.{Lock, Modifying, QueryHints}
import org.springframework.data.util.Streamable
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.{afford, linkTo}
import org.springframework.hateoas.{CollectionModel, EntityModel, Link, LinkRelation}
import org.springframework.http.{HttpMethod, HttpStatus, ResponseEntity}
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Collectors.toList
import java.util.stream.IntStream
import javax.persistence.{LockModeType, QueryHint}
import javax.transaction.Transactional

@Service
class RecipeService extends IRecipeService {

  private final val PERSISTENCE_LOCK_TIMEOUT = "javax.persistence.lock.timeout"
  private final val QUERY_READ_TIMEOUT = "${service.query_read_timeout:3000}"
  private final val QUERY_WRITE_TIMEOUT = "${service.query_write_timeout:3000}"

  @Autowired
  private var recipeRepository: RecipeRepository = _

  @Autowired
  private var recipeSearchRepository: RecipeSearchRepository = _

  @Autowired
  private var elasticsearchClient: ElasticsearchClient = _

  @Transactional
  @Lock(LockModeType.READ)
  @QueryHints(
    Array(
      QueryHint(name = PERSISTENCE_LOCK_TIMEOUT, value = QUERY_READ_TIMEOUT)
    )
  )
  def getAllRecipes(start: Int, limit: Int): Page[Recipe] =
    recipeRepository.findAll(PageRequest.of(start, limit))

  @Transactional
  @Lock(LockModeType.READ)
  @QueryHints(
    Array(
      QueryHint(name = PERSISTENCE_LOCK_TIMEOUT, value = QUERY_READ_TIMEOUT)
    )
  )
  def recipeExistsById(id: Long): Boolean = recipeRepository.existsById(id)

  @Transactional
  @Lock(LockModeType.READ)
  @QueryHints(
    Array(
      QueryHint(name = PERSISTENCE_LOCK_TIMEOUT, value = QUERY_READ_TIMEOUT)
    )
  )
  def getRecipeById(id: Long): Optional[Recipe] = recipeRepository.findById(id)

  @Transactional
  @Modifying(flushAutomatically = true)
  @Lock(LockModeType.WRITE)
  @QueryHints(
    Array(
      QueryHint(
        name = PERSISTENCE_LOCK_TIMEOUT,
        value = QUERY_WRITE_TIMEOUT
      )
    )
  )
  def addRecipe(recipe: Recipe): Recipe = {
    val recipes: Streamable[Recipe] =
      recipeRepository.findAllByName(recipe.name)

    recipe.variation =
      recipes.stream.mapToInt(recipe => recipe.variation).max.orElse(0) + 1
    recipe.creationDateTime = LocalDateTime.now
    recipe.lastModifiedDateTime = recipe.creationDateTime
    IntStream
      .range(0, recipe.ingredients.size)
      .forEach(idx => recipe.ingredients.get(idx).ingredientNumber = idx + 1)
    IntStream
      .range(0, recipe.instructions.size)
      .forEach(idx => recipe.instructions.get(idx).instructionNumber = idx + 1)
    val savedRecipe = recipeRepository.save(recipe)
    recipeSearchRepository.save(savedRecipe.asRecipeDoc)
    savedRecipe
  }

  @Transactional
  @Modifying(flushAutomatically = true)
  @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
  @QueryHints(
    Array(
      QueryHint(
        name = PERSISTENCE_LOCK_TIMEOUT,
        value = QUERY_WRITE_TIMEOUT
      )
    )
  )
  def updateRecipe(newRecipe: Recipe): Recipe = {
    val optionalRecipe = recipeRepository.findById(newRecipe.recipeId)

    if (optionalRecipe.isEmpty) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    newRecipe.creationDateTime = optionalRecipe.get.creationDateTime
    newRecipe.lastModifiedDateTime = LocalDateTime.now
    IntStream
      .range(0, newRecipe.ingredients.size)
      .forEach(idx =>
        newRecipe.ingredients.get(idx).ingredientNumber = idx + 1
      )
    IntStream
      .range(0, newRecipe.instructions.size)
      .forEach(idx =>
        newRecipe.instructions.get(idx).instructionNumber = idx + 1
      )
    val savedRecipe = recipeRepository.save(newRecipe)
    recipeSearchRepository.save(savedRecipe.asRecipeDoc)
    savedRecipe
  }

  @Transactional
  @Modifying(flushAutomatically = true)
  @Lock(LockModeType.WRITE)
  @QueryHints(
    Array(
      QueryHint(
        name = PERSISTENCE_LOCK_TIMEOUT,
        value = QUERY_WRITE_TIMEOUT
      )
    )
  )
  def deleteRecipeById(id: Long): Unit = recipeRepository.deleteById(id)

  def searchRecipes(searchText: String): ResponseBody[RecipeDoc] = {
    val queryString = new QueryStringQuery.Builder().query(searchText).build
    val query = new Query.Builder().queryString(queryString).build
    val searchRequest =
      new SearchRequest.Builder().index("recipes").query(query).build
    elasticsearchClient.search(searchRequest, classOf[RecipeDoc])
  }
}
