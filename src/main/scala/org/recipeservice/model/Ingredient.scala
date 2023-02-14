package org.recipeservice.model

import javax.persistence.{CascadeType, Column, Entity, EnumType, Enumerated, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table, UniqueConstraint}
import javax.validation.constraints.NotNull

@Entity
@Table(name = "ingredients", uniqueConstraints = Array(UniqueConstraint(name = "unique_ingredient_recipe_and_order", columnNames = Array("recipe_id", "ingredient_number"))))
class Ingredient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recipe_id")
  var recipeId: Long = _

  @Column(name = "ingredient_number", columnDefinition="INT CONSTRAINT positive_ingredient_number CHECK (ingredient_number > 0)")
  var ingredientNumber: Int = _

  @Column(name = "quantity_specifier")
  @Enumerated(EnumType.STRING)
  @NotNull
  var quantitySpecifier: QuantitySpecifier = _

  @Column(name = "quantity", precision=10, scale=2)
  @NotNull
  var quantity: BigDecimal = _

  @Column(name = "ingredient", length=256)
  @NotNull
  var ingredient: String = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "recipe_id")
  private val recipe: Recipe = null
}
