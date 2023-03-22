package org.recipeservice.services

import org.recipeservice.model.{Ingredient, Recipe}
import org.recipeservice.repository.{RecipeRepository, RecipeSearchRepository}

import java.util.Optional
import javax.transaction.Transactional
import org.recipeservice.controller.{RecipeController, RecipeResourceAssembler}
import org.recipeservice.document.RecipeDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.data.jpa.repository.{Lock, Modifying, QueryHints}
import org.springframework.data.util.Streamable
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.{CollectionModel, EntityModel, Link, LinkRelation}
import org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford
import org.springframework.http.{HttpMethod, ResponseEntity}

import java.util.stream.Collectors.toList
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

import java.time.LocalDateTime
import java.util.stream.IntStream
import javax.persistence.{LockModeType, QueryHint}

@Service
class RecipeService extends IRecipeService {

    @Autowired
    var recipeRepository: RecipeRepository = _

    @Autowired
    var recipeSearchRepository: RecipeSearchRepository = _

    @Transactional
    @Lock(LockModeType.READ)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_read_timeout:3000}")))
    def getAllRecipes(start: Int, limit: Int): Page[Recipe] = recipeRepository.findAll(PageRequest.of(start, limit))

    @Transactional
    @Lock(LockModeType.READ)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_read_timeout:3000}")))
    def recipeExistsById(id: Long): Boolean = recipeRepository.existsById(id)

    @Transactional
    @Lock(LockModeType.READ)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_read_timeout:3000}")))
    def getRecipeById(id: Long): Optional[Recipe] = recipeRepository.findById(id)

    @Transactional
    @Modifying(flushAutomatically = true)
    @Lock(LockModeType.WRITE)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_write_timeout:3000}")))
    def addRecipe(recipe: Recipe): Recipe = {
        val recipes: Streamable[Recipe] = recipeRepository.findAllByName(recipe.name)

        recipe.variation = recipes.stream().mapToInt(recipe => recipe.variation).max().orElse(0) + 1
        recipe.creationDateTime = LocalDateTime.now()
        recipe.lastModifiedDateTime = recipe.creationDateTime
        IntStream
          .range(0, recipe.ingredients.size())
          .forEach(idx => recipe.ingredients.get(idx).ingredientNumber = idx + 1);
        IntStream
          .range(0, recipe.instructions.size())
          .forEach(idx => recipe.instructions.get(idx).instructionNumber = idx + 1);
        val savedRecipe = recipeRepository.save(recipe)
        val recipeDoc = recipeSearchRepository.save(RecipeDoc.create(savedRecipe))
        savedRecipe
    }

    @Transactional
    @Modifying(flushAutomatically = true)
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_write_timeout:3000}")))
    def updateRecipe(newRecipe: Recipe): Recipe = {
        var optionalRecipe = recipeRepository.findById(newRecipe.recipeId)

        if (optionalRecipe.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND)

        newRecipe.creationDateTime = optionalRecipe.get().creationDateTime
        newRecipe.lastModifiedDateTime = LocalDateTime.now()
        IntStream
          .range(0, newRecipe.ingredients.size())
          .forEach(idx => newRecipe.ingredients.get(idx).ingredientNumber = idx + 1);
        IntStream
          .range(0, newRecipe.instructions.size())
          .forEach(idx => newRecipe.instructions.get(idx).instructionNumber = idx + 1);
        val savedRecipe = recipeRepository.save(newRecipe)
        val recipeDoc = recipeSearchRepository.save(RecipeDoc.create(savedRecipe))
        savedRecipe
    }


    @Transactional
    @Modifying(flushAutomatically = true)
    @Lock(LockModeType.WRITE)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_write_timeout:3000}")))
    def deleteRecipeById(id: Long): Unit = recipeRepository.deleteById(id)
}
