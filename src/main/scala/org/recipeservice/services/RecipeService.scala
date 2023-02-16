package org.recipeservice.services

import org.recipeservice.model.{Ingredient, Recipe}
import org.recipeservice.repository.RecipeRepository

import java.util.Optional
import javax.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.{Lock, Modifying, QueryHints}
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

    @Transactional
    @Lock(LockModeType.READ)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_read_timeout:3000}")))
    def getAllRecipes(start: Int, limit: Int): java.util.List[Recipe] = recipeRepository.findAll(PageRequest.of(start, limit)).getContent()

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
        recipe.creationDateTime = LocalDateTime.now()
        recipe.lastModifiedDateTime = recipe.creationDateTime
        IntStream
          .range(0, recipe.ingredients.size())
          .forEach(idx => recipe.ingredients.get(idx).ingredientNumber = idx + 1);
        IntStream
          .range(0, recipe.instructions.size())
          .forEach(idx => recipe.instructions.get(idx).instructionNumber = idx + 1);
        recipeRepository.save(recipe)
    }

    @Transactional
    @Modifying(flushAutomatically = true)
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_write_timeout:3000}")))
    def updateRecipe(newRecipe: Recipe): Recipe = {
        var optionalRecipe = recipeRepository.findById(newRecipe.recipeId)

        if (optionalRecipe.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND)

        newRecipe.lastModifiedDateTime = LocalDateTime.now()
        IntStream
          .range(0, newRecipe.ingredients.size())
          .forEach(idx => newRecipe.ingredients.get(idx).ingredientNumber = idx + 1);
        IntStream
          .range(0, newRecipe.instructions.size())
          .forEach(idx => newRecipe.instructions.get(idx).instructionNumber = idx + 1);
        recipeRepository.save(newRecipe)
    }


    @Transactional
    @Modifying(flushAutomatically = true)
    @Lock(LockModeType.WRITE)
    @QueryHints(Array(QueryHint(name = "javax.persistence.lock.timeout", value = "${service.query_write_timeout:3000}")))
    def deleteRecipeById(id: Long): Unit = recipeRepository.deleteById(id)
}
