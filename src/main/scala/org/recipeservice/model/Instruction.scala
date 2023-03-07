package org.recipeservice.model

import io.swagger.v3.oas.annotations.media.Schema

import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, ManyToOne, Table, UniqueConstraint}
import javax.validation.constraints.NotNull

@Entity
@Schema(name = "Instruction")
@Table(name = "instructions")
class Instruction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "instruction_id")
  @Schema(description = "The generated ID when saved in database", name = "instructionId")
  var instructionId: Long = _

  @Column(name = "instruction_number", columnDefinition="INT CONSTRAINT positive_instruction_number CHECK (instruction_number > 0)")
  @Schema(description = "The order for the instruction", name = "instructionNumber")
  var instructionNumber: Int = _

  @Column(name = "instruction")
  @NotNull
  @Schema(description = "The instruction", name = "instruction")
  var instruction: String = _

  @ManyToOne(cascade = Array(CascadeType.ALL))
  @JoinColumn(name = "recipe_id")
  private val recipe: Recipe = null
}
