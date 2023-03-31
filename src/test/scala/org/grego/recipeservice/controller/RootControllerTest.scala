package org.grego.recipeservice.controller

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.{Disabled, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort

class RootControllerTest extends BaseAppTest {
  @Autowired
  var rootController: RootController = _

  @Test
  def testRootEndpoint(): Unit = {
    val entity = restTemplate.getForObject(s"http://localhost:$port/", classOf[Map[String, Any]])
    assertTrue(entity.contains("name"))
    assertTrue(entity.contains("message"))
  }
}
