package org.recipeservice.repository

import org.recipeservice.model.Ingredient
import org.springframework.data.repository.CrudRepository

trait IngredientRepository extends CrudRepository[Ingredient, Long] {

}
