package org.recipeservice.model

import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table, UniqueConstraint}
import javax.validation.constraints.NotNull

@Entity
@Table(name = "instructions", uniqueConstraints = Array(UniqueConstraint(name = "unique_instruction_recipe_and_order", columnNames = Array("recipe_id", "instruction_number"))))
class Instruction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recipe_id")
  var recipeId: Long = _

  @Column(name = "instruction_number", columnDefinition="INT CONSTRAINT positive_instruction_number CHECK (instruction_number > 0)")
  var instructionNumber: Int = _

  @Column(name = "instruction")
  @NotNull
  var instruction: String = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "recipe_id")
  private val recipe: Recipe = null
}
