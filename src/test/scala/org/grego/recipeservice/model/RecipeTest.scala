package org.grego.recipeservice.model

import org.junit.jupiter.api.{Tag, Test, TestInstance}
import com.google.code.beanmatchers.BeanMatchers.{hasValidBeanConstructor, hasValidBeanEquals, hasValidBeanHashCode, hasValidBeanToString}
import com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals, assertNull}

import java.util.{List, Map, Random}
import com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals, assertNull}

import java.time.LocalDateTime
import java.util
import java.util.Random
import java.util.Map

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("UnitTests")
class RecipeTest {

  /**
   * Test Recipe class.
   */
  @Test def testRecipe(): Unit = {
    // Test no arguments constructor, getters, setters, hashCode, equals, and toString.
    assertThat(classOf[Recipe], hasValidBeanConstructor)
    assertThat(classOf[Recipe], hasValidGettersAndSetters)
    assertThat(classOf[Recipe], hasValidBeanHashCode)
    assertThat(classOf[Recipe], hasValidBeanEquals)
    assertThat(classOf[Recipe], hasValidBeanToString)
    // Test all arguments constructor and builder
    val random: Random = new Random
    val recipeId: Long = random.nextLong
    val name: String = RandomStringUtils.randomAlphabetic(20)
    val variation: Int = random.nextInt
    val description: String = RandomStringUtils.randomAlphabetic(30)
    val creationDateTime: LocalDateTime = Instancio.create(classOf[LocalDateTime])
    val lastModifiedDateTime: LocalDateTime = Instancio.create(classOf[LocalDateTime])
    val ingredients: util.List[Ingredient] = Instancio.ofList(classOf[Ingredient]).size(2).create
    val instructions = Instancio.ofList(classOf[Instruction]).size(2).create
    val testRecipe: Recipe = new Recipe
    testRecipe.recipeId = recipeId
    testRecipe.name = name
    testRecipe.variation = variation
    testRecipe.description = description
    testRecipe.creationDateTime = creationDateTime
    testRecipe.lastModifiedDateTime = lastModifiedDateTime
    testRecipe.ingredients = ingredients
    testRecipe.instructions = instructions
    assertNotEquals(testRecipe, Instancio.create(classOf[Recipe]))
    assertNotEquals(testRecipe, testRecipe.asRecipeDoc)
  }
}
