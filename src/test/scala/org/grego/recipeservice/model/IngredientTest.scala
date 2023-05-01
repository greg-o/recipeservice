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
class IngredientTest {

  /**
   * Test Ingredient class.
   */
  @Test def testIngredient(): Unit = {
    // Test no arguments constructor, getters, setters, hashCode, equals, and toString.
    assertThat(classOf[Ingredient], hasValidBeanConstructor)
    assertThat(classOf[Ingredient], hasValidGettersAndSetters)
    assertThat(classOf[Ingredient], hasValidBeanHashCode)
    assertThat(classOf[Ingredient], hasValidBeanEquals)
    assertThat(classOf[Ingredient], hasValidBeanToString)
    // Test all arguments constructor and builder
    val random: Random = new Random
    val ingredientId: Long = random.nextLong
    val ingredientNumber: Int = random.nextInt
    val quantitySpecifier: QuantitySpecifier = QuantitySpecifier.Unspecified
    val quantity: Double = random.nextDouble
    val ingredient: String = RandomStringUtils.randomAlphabetic(20)
    val testIngredient: Ingredient = new Ingredient
    testIngredient.ingredientId = ingredientId
    testIngredient.ingredientNumber = ingredientNumber
    testIngredient.quantitySpecifier = quantitySpecifier
    testIngredient.quantity = quantity
    testIngredient.ingredient = ingredient
    assertNotEquals(testIngredient, Instancio.create(classOf[Ingredient]))
    assertNotEquals(testIngredient, testIngredient.asIngredientDoc)
  }
}
