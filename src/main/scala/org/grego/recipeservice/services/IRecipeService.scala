// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.services

import co.elastic.clients.elasticsearch.core.search.ResponseBody
import org.grego.recipeservice.document.RecipeDoc
import org.grego.recipeservice.model.Recipe
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

  def searchRecipes(searchText: String): ResponseBody[RecipeDoc]
}
