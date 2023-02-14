package org.recipeservice.services

import org.recipeservice.model.{Ingredient, Recipe}
import org.recipeservice.repository.RecipeRepository

import java.util.Optional
import javax.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

import java.util.stream.IntStream


@Service
class RecipeService extends IRecipeService {

    @Autowired
    var recipeRepository: RecipeRepository = _

    @Transactional
    def getAllRecipes(start: Int, limit: Int): java.util.List[Recipe] = recipeRepository.findAll(PageRequest.of(start, limit)).getContent()

    @Transactional
    def recipeExistsById(id: Long): Boolean = recipeRepository.existsById(id)


    @Transactional
    def getRecipeById(id: Long): Optional[Recipe] = recipeRepository.findById(id)

    @Modifying(flushAutomatically = true)
    def addRecipe(recipe: Recipe): Recipe = {
        IntStream
          .range(0, recipe.ingredients.size())
          .forEach(idx => recipe.ingredients.get(idx).ingredientNumber = idx + 1);
        IntStream
          .range(0, recipe.instructions.size())
          .forEach(idx => recipe.instructions.get(idx).instructionNumber = idx + 1);
        recipeRepository.save(recipe)
    }

    @Modifying(flushAutomatically = true)
    def updateRecipe(newRecipe: Recipe): Recipe = {
        var optionalRecipe = recipeRepository.findById(newRecipe.recipeId)

        if (optionalRecipe.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND)

        IntStream
          .range(0, newRecipe.ingredients.size())
          .forEach(idx => newRecipe.ingredients.get(idx).ingredientNumber = idx + 1);
        IntStream
          .range(0, newRecipe.instructions.size())
          .forEach(idx => newRecipe.instructions.get(idx).instructionNumber = idx + 1);
        recipeRepository.save(newRecipe)
    }


    @Modifying(flushAutomatically = true)
    def deleteRecipeById(id: Long): Unit = recipeRepository.deleteById(id)
}
