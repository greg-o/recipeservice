// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.controller

import io.micrometer.core.annotation.Timed
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.{CacheControl, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation.{GetMapping, RestController}

import java.time.LocalDateTime
import scala.io.Source

@RestController
class BuildInfoController {
  private def loadBuildInfo(): String =
    Source
      .fromInputStream(
        getClass.getClassLoader.getResourceAsStream("build-info.json")
      )
      .getLines
      .mkString

  @Timed
  @GetMapping(
    path = Array("/buildInfo"),
    produces = Array(MediaType.APPLICATION_JSON_VALUE)
  )
  def get(): ResponseEntity[String] =
    ResponseEntity.ok()
      .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
      .cacheControl(CacheControl.noStore())
      .body(loadBuildInfo())
}
