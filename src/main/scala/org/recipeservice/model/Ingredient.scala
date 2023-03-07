package org.recipeservice.model

import javax.persistence.{CascadeType, Column, Entity, EnumType, Enumerated, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table, UniqueConstraint}
import javax.validation.constraints.NotNull
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.media.Schema

@Entity
@Schema(name = "Ingredient")
@Table(name = "ingredients")
class Ingredient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ingredient_id")
  @Schema(description = "The generated ID when saved in database", name = "ingredientId")
  var ingredientId: Long = _

  @Column(name = "ingredient_number", columnDefinition="INT CONSTRAINT positive_ingredient_number CHECK (ingredient_number > 0)")
  @Schema(description = "The order for the ingredient", name = "ingredientNumber")
  var ingredientNumber: Int = _

  @Column(name = "quantity_specifier")
  @Enumerated(EnumType.STRING)
  @NotNull
  @Schema(description = "The specifier of the quantity", name = "quantitySpecifier")
  var quantitySpecifier: QuantitySpecifier = _

  @Column(name = "quantity", columnDefinition="Decimal(10,2)")
  @NotNull
  @Schema(description = "The quantity f the ingredient", name = "quantity")
  var quantity: Double = _

  @Column(name = "ingredient", length=256)
  @NotNull
  @Schema(description = "The ingredient", name = "ingredient")
  var ingredient: String = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "recipe_id")
  private val recipe: Recipe = null
}
