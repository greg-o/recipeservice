// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.controller

import io.micrometer.core.annotation.Timed
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{MediaType, ResponseEntity}
import org.springframework.web.bind.annotation.*

import java.time.LocalDateTime

@RestController
@RequestMapping(path = Array("/probe"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
class ProbeController {

  @Timed
  @GetMapping(path = Array("/live"))
  def live(): ResponseEntity[Map[String, Any]] =
    ResponseEntity.ok().body(Map("message" -> "I'm alive!"))

  @Timed
  @GetMapping(path = Array("/ready"))
  def ready(): ResponseEntity[Map[String, Any]] =
    ResponseEntity.ok().body(Map("message" -> "I'm ready!"))
}
