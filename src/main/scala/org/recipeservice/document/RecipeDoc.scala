package org.recipeservice.document

import com.fasterxml.jackson.annotation.JsonFormat
import org.recipeservice.model.{Ingredient, Instruction, Recipe}

import java.time.{LocalDateTime, ZoneOffset}
import org.springframework.data.elasticsearch.annotations.{DateFormat, Document, Field, FieldType}

import java.util.Date

@Document(indexName = "recipes")
class RecipeDoc(var id: Long, var name: String, var variation: Int, var description: String,
                @Field(`type` = FieldType.Date, format = Array(DateFormat.date_hour_minute_second_fraction))
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
                var creationDateTime: Date,
                @Field(`type` = FieldType.Date, format = Array(DateFormat.date_hour_minute_second_fraction))
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
                var lastModifiedDateTime: Date,
                var ingredients: java.util.List[Ingredient], var instructions: java.util.List[Instruction]) {
}

object RecipeDoc {
  def create(recipe: Recipe) = {
    new RecipeDoc(recipe.recipeId, recipe.name, recipe.variation, recipe.description,
      Date.from(recipe.creationDateTime.toInstant(ZoneOffset.UTC)),
      Date.from(recipe.lastModifiedDateTime.toInstant(ZoneOffset.UTC)),
      recipe.ingredients, recipe.instructions)
  }
}