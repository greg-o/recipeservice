package org.grego.recipeservice.model

import org.junit.jupiter.api.{Tag, Test, TestInstance}
import com.google.code.beanmatchers.BeanMatchers.{hasValidBeanConstructor, hasValidBeanEquals, hasValidBeanHashCode, hasValidBeanToString}
import com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals, assertNull}

import java.util.Random
import java.util.Map

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("UnitTests")
class InstructionTest {

  /**
   * Length of the instruction.
   */
  val INSTRUCTION_LENGTH = 30

  /**
   * Test Instruction class.
   */
  @Test def testInstruction(): Unit = {
    // Test no arguments constructor, getters, setters, hashCode, equals, and toString.
    assertThat(classOf[Instruction], hasValidBeanConstructor)
    assertThat(classOf[Instruction], hasValidGettersAndSetters)
    assertThat(classOf[Instruction], hasValidBeanHashCode)
    assertThat(classOf[Instruction], hasValidBeanEquals)
    assertThat(classOf[Instruction], hasValidBeanToString)
    // Test all arguments constructor and builder
    val random: Random = new Random
    val instructionId: Long = random.nextLong
    val instructionNumber: Int = random.nextInt
    val instruction: String = RandomStringUtils.randomAlphabetic(INSTRUCTION_LENGTH)
    val testInstruction: Instruction = new Instruction
    testInstruction.instructionId = instructionId
    testInstruction.instructionNumber = instructionNumber
    testInstruction.instruction = instruction
    assertNotEquals(testInstruction, Instancio.create(classOf[Instruction]))
    assertNotEquals(testInstruction, testInstruction.asInstructionDoc)
  }
}
