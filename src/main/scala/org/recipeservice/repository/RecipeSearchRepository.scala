package org.recipeservice.repository

import org.recipeservice.document.RecipeDoc
import org.recipeservice.model.Recipe
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

trait RecipeSearchRepository extends ElasticsearchRepository[RecipeDoc, Long] {

}
