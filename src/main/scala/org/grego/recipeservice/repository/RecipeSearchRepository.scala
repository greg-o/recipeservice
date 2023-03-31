// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.repository

import org.grego.recipeservice.document.RecipeDoc
import org.grego.recipeservice.model.Recipe
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

trait RecipeSearchRepository extends ElasticsearchRepository[RecipeDoc, Long]
