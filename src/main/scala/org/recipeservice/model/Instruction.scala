package org.recipeservice.model

import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table, UniqueConstraint}
import javax.validation.constraints.NotNull

@Entity
@Table(name = "instructions")
class Instruction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "instruction_id")
  var instructionId: Long = _

  @Column(name = "instruction_number", columnDefinition="INT CONSTRAINT positive_instruction_number CHECK (instruction_number > 0)")
  var instructionNumber: Int = _

  @Column(name = "instruction")
  @NotNull
  var instruction: String = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "recipe_id")
  private val recipe: Recipe = null
}
