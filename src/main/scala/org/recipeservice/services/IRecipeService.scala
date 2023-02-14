package org.recipeservice.services

import org.recipeservice.model.Recipe
import java.util.Optional
trait IRecipeService {

  def getAllRecipes(start: Int, limit: Int): java.util.List[Recipe]

  def recipeExistsById(id: Long): Boolean

  def getRecipeById(id: Long): Optional[Recipe]

  def addRecipe(recipe: Recipe): Recipe

  def updateRecipe(recipe: Recipe): Recipe

  def deleteRecipeById(id: Long): Unit
}
