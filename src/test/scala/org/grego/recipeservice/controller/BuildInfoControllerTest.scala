package org.grego.recipeservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.`type`.TypeReference

import java.util.stream.Collectors
import java.util.{Collections, UUID}

import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.trace.http.HttpTrace.Response
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles

class BuildInfoControllerTest extends BaseAppTest {
  @Autowired
  var buildInfoController: BuildInfoController = _

  @Test
  def testGetBuildInfo(): Unit = {
    val buildInfoString = restTemplate.getForObject(s"http://localhost:$port/buildInfo", classOf[String])
    val buildInfoMap = new ObjectMapper().readValue(buildInfoString,
      new TypeReference[java.util.Map[String, Object]]() {})

    assertTrue(buildInfoMap.get("version").toString.nonEmpty)
    assertTrue(buildInfoMap.get("build-time").toString.nonEmpty)
    assertTrue(buildInfoMap.get("branch").toString.nonEmpty)
  }
}
