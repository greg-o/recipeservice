package org.recipeservice.services

import org.recipeservice.model.Recipe
import org.springframework.data.domain.Page
import org.springframework.hateoas.{CollectionModel, EntityModel}
import org.springframework.http.ResponseEntity

import java.util.Optional
trait IRecipeService {

  def getAllRecipes(start: Int, limit: Int): Page[Recipe]
  def recipeExistsById(id: Long): Boolean

  def getRecipeById(id: Long): Optional[Recipe]

  def addRecipe(recipe: Recipe): Recipe

  def updateRecipe(recipe: Recipe): Recipe

  def deleteRecipeById(id: Long): Unit
}
