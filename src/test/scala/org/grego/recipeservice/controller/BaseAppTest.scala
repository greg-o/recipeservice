package org.grego.recipeservice.controller

import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.trace.http.HttpTrace.Response
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.{ActiveProfiles, TestPropertySource}

import java.util.{Collections, UUID}

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test"))
@TestPropertySource(
  properties =
    Array("spring.config.additional-location=classpath:application-test.yml")
)
class BaseAppTest {
  @LocalServerPort
  val port = 0
  @Autowired
  var restTemplate: TestRestTemplate = _
}
