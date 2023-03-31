// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.controller

import io.micrometer.core.annotation.Timed
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.{GetMapping, RestController}

import java.time.LocalDateTime

@RestController
class RootController {
  @Value("${application.name}")
  private var appName: String = _

  @GetMapping(path = Array("/"))
  @Timed
  def root(): Map[String, Any] = Map("name" -> appName, "message" -> "It works on my machine!")
}
