// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.model

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.media.Schema
import org.grego.recipeservice.document.IngredientDoc

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Schema(name = "Ingredient")
@Table(name = "ingredients")
class Ingredient {

  private final val INGREDIENT_FIELD_LENGTH = 256

  @ManyToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "recipe_id")
  private var recipe: Recipe = _
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ingredient_id")
  @Schema(
    description = "The generated ID when saved in database",
    name = "ingredientId"
  )
  var ingredientId: Long = _
  @Column(
    name = "ingredient_number",
    columnDefinition =
      "INT CONSTRAINT positive_ingredient_number CHECK (ingredient_number > 0)"
  )
  @Schema(
    description = "The order for the ingredient",
    name = "ingredientNumber"
  )
  var ingredientNumber: Int = _
  @Column(name = "quantity_specifier")
  @Enumerated(EnumType.STRING)
  @NotNull
  @Schema(
    description = "The specifier of the quantity",
    name = "quantitySpecifier"
  )
  var quantitySpecifier: QuantitySpecifier = _
  @Column(name = "quantity", columnDefinition = "Decimal(10,2)")
  @NotNull
  @Schema(description = "The quantity f the ingredient", name = "quantity")
  var quantity: Double = _
  @Column(name = "ingredient", length = INGREDIENT_FIELD_LENGTH)
  @NotNull
  @Schema(description = "The ingredient", name = "ingredient")
  var ingredient: String = _

  def asIngredientDoc: IngredientDoc = {
    new IngredientDoc(ingredientId, ingredientNumber, quantitySpecifier, quantity, ingredient)
  }
}

object Ingredient {
  def create(ingredient: String, specifier: QuantitySpecifier, quantity: Double): Ingredient = {
    var i = new Ingredient
    i.ingredient = ingredient
    i.quantitySpecifier = specifier
    i.quantity = quantity
    i
  }
}
