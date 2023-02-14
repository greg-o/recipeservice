package org.recipeservice.repository

import org.recipeservice.model.Recipe
import org.springframework.data.repository.PagingAndSortingRepository
trait RecipeRepository extends PagingAndSortingRepository[Recipe, Long] {

}
