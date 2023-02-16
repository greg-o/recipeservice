package org.recipeservice.repository

import org.recipeservice.model.Instruction
import org.springframework.data.repository.CrudRepository

trait InstructionRepository extends CrudRepository[Instruction, Long] {

}
