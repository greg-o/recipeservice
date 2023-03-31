// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.controller

import co.elastic.clients.elasticsearch.core.search.ResponseBody
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.tools.javac.util.DefinedBy.Api
import de.ingogriebsch.spring.hateoas.siren.MediaTypes
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.info.{Contact, Info}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.{ApiResponse, ApiResponses}
import io.swagger.v3.oas.annotations.{OpenAPIDefinition, Operation}
import org.grego.recipeservice.document.RecipeDoc
import org.grego.recipeservice.model.Recipe
import org.grego.recipeservice.repository.RecipeRepository
import org.grego.recipeservice.services.IRecipeService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.data.domain.Page
import org.springframework.hateoas.{EntityModel, PagedModel}
import org.springframework.hateoas.PagedModel.PageMetadata
import org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.{afford, linkTo}
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

import java.io.StringWriter
import java.util.Optional

@RestController
@OpenAPIDefinition(
  info = Info(
    title = "Recipe Service APIs",
    version = "1.0.0",
    description = "Recipe REST APIs",
    contact = Contact(email = "greg.osgood@gmail.com")
  )
)
@RequestMapping(
  path = Array("/recipes"),
  produces =
    Array(MediaTypes.SIREN_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE)
)
class RecipeController {

  private final val RESPONSE_SUCCESS = "200"
  private final val RESPONSE_INTERNAL_SERVER_ERRPR = "500"

  private final val RESPONSE_SUCCESS_MESSAGE = "The request has succeeded."
  private final val RESPONSE_INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error."

  private final val INCLUDE_HYPER_LINKS = "include-hyper-links"
  private final val VALUE_FALSE = "false"

  @Autowired
  var recipeResourceAssembler: RecipeResourceAssembler = _
  @Autowired
  private var recipeService: IRecipeService = _
  @Value("${service.default_page_size:20}")
  private var defaultPageSize: String = _

  @Autowired
  private var objectMapper: ObjectMapper = _

  @Timed
  @Operation(
    summary = "Returns list of recipes",
    description = "This service returns a list of recipes",
    method = "listRecipes",
    responses = Array(
      ApiResponse(
        responseCode = RESPONSE_SUCCESS,
        description = RESPONSE_SUCCESS_MESSAGE,
        content = Array(
          Content(
            mediaType = MediaTypes.SIREN_JSON_VALUE,
            schema = Schema(implementation = classOf[Recipe])
          )
        )
      ),
      ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERRPR, description = RESPONSE_INTERNAL_SERVER_ERROR_MESSAGE)
    )
  )
  @GetMapping(path = Array("/list"))
  def listRecipes(
      @RequestParam(name = "page-number") page: Optional[String],
      @RequestParam(name = "page-size") size: Optional[String],
      @RequestParam(
        name = INCLUDE_HYPER_LINKS,
        defaultValue = VALUE_FALSE
      ) includeHyperLinks: Boolean
  ): ResponseEntity[?] = {
    val pageNumber = Integer.parseInt(page.orElse("0"))
    val pageSize = Integer.parseInt(size.orElse(defaultPageSize))
    val recipesPage = recipeService.getAllRecipes(pageNumber, pageSize)

    if (includeHyperLinks) {
      ResponseEntity
        .ok
        .contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON)
        .body(addHyperLinks(pageNumber, pageSize, recipesPage))
    } else {
      ResponseEntity
        .ok
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(recipesPage.getContent)
    }
  }

  @Timed
  @Operation(
    summary = "Returns list a recipe",
    description = "This service returns a recipe for the given id",
    method = "getRecipe",
    responses = Array(
      ApiResponse(
        responseCode = RESPONSE_SUCCESS,
        description = RESPONSE_SUCCESS_MESSAGE,
        content = Array(
          Content(
            mediaType = MediaTypes.SIREN_JSON_VALUE,
            schema = Schema(implementation = classOf[Recipe])
          )
        )
      ),
      ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERRPR, description = RESPONSE_INTERNAL_SERVER_ERROR_MESSAGE)
    )
  )
  @GetMapping(path = Array("/get/{id}"))
  def getRecipe(
      @PathVariable("id") id: Long,
      @RequestParam(
        name = INCLUDE_HYPER_LINKS,
        defaultValue = VALUE_FALSE
      ) includeHyperLinks: Boolean
  ): ResponseEntity[?] = {
    val recipe = recipeService.getRecipeById(id)

    if (recipe.isEmpty) {
      ResponseEntity.notFound.build
    } else {
      if (includeHyperLinks) {
        ResponseEntity
          .ok
          .contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON)
          .body(recipeResourceAssembler.toModel(recipe.get))
      } else {
        ResponseEntity
          .ok
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
          .body(recipe.get)
      }
    }
  }

  @Timed
  @Operation(
    summary = "Adds a recipe",
    description = "This service adds a recipe",
    method = "addRecipe",
    responses = Array(
      ApiResponse(
        responseCode = RESPONSE_SUCCESS,
        description = RESPONSE_SUCCESS_MESSAGE,
        content = Array(
          Content(
            mediaType = MediaTypes.SIREN_JSON_VALUE,
            schema = Schema(implementation = classOf[Recipe])
          )
        )
      ),
      ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERRPR, description = RESPONSE_INTERNAL_SERVER_ERROR_MESSAGE)
    )
  )
  @PutMapping(path = Array("/add"))
  def addRecipe(
      @RequestBody recipe: Recipe,
      @RequestParam(
        name = INCLUDE_HYPER_LINKS,
        defaultValue = VALUE_FALSE
      ) includeHyperLinks: Boolean
  ): ResponseEntity[?] = {
    val addedRecipe = recipeService.addRecipe(recipe)
    if (includeHyperLinks) {
      ResponseEntity
        .ok
        .contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON)
        .body(recipeResourceAssembler.toModel(addedRecipe))
    } else {
      ResponseEntity
        .ok
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(addedRecipe)
    }
  }

  @Timed
  @Operation(
    summary = "Updates a recipe",
    description = "This service updates a recipe",
    method = "updateRecipe",
    responses = Array(
      ApiResponse(
        responseCode = RESPONSE_SUCCESS,
        description = RESPONSE_SUCCESS_MESSAGE
      ),
      ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERRPR, description = RESPONSE_INTERNAL_SERVER_ERROR_MESSAGE)
    )
  )
  @PatchMapping(path = Array("/update"))
  def updateRecipe(
      @RequestBody recipe: Recipe,
      @RequestParam(
        name = INCLUDE_HYPER_LINKS,
        defaultValue = VALUE_FALSE
      ) includeHyperLinks: Boolean
  ): ResponseEntity[?] = {
    val updatedRecipe = recipeService.updateRecipe(recipe)
    if (includeHyperLinks) {
      ResponseEntity
        .ok
        .contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON)
        .body(recipeResourceAssembler.toModel(updatedRecipe))
    } else {
      ResponseEntity
        .ok
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(updatedRecipe)
    }
  }

  @Timed
  @Operation(
    summary = "Deletes a recipe",
    description = "This service deletes a recipe",
    method = "deleteRecipe",
    responses = Array(
      ApiResponse(
        responseCode = RESPONSE_SUCCESS,
        description = RESPONSE_SUCCESS_MESSAGE
      ),
      ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERRPR, description = RESPONSE_INTERNAL_SERVER_ERROR_MESSAGE)
    )
  )
  @DeleteMapping(path = Array("/delete/{id}"))
  def deleteRecipe(@PathVariable("id") id: Long): ResponseEntity[?] = {
    if (!recipeService.recipeExistsById(id)) {
      ResponseEntity.notFound.build
    } else {
      recipeService.deleteRecipeById(id)
      ResponseEntity.ok("Delete recipe: id = " + id)
    }
  }

  @Timed
  @GetMapping(
    path = Array("/search"),
    produces = Array(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
  )
  def searchRecipes(
      @RequestParam(
        value = "search-string",
        required = true
      ) searchString: String
  ): ResponseEntity[?] = {
    val results = recipeService.searchRecipes(searchString)
    val writer = new StringWriter
    val jacksonJsonpMapper = new JacksonJsonpMapper(objectMapper)

    try {
      val generator = jacksonJsonpMapper.jsonProvider.createGenerator(writer)
      try results.serialize(generator, jacksonJsonpMapper)
      finally if (generator != null) generator.close
    }
    ResponseEntity
      .ok
      .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
      .body(writer.toString)
  }

  private def addHyperLinks(pageNumber: Int, pageSize: Int, recipesPage: Page[Recipe]) = {
    val recipeCollectionModel =
      recipeResourceAssembler.toCollectionModel(recipesPage.getContent)
    val metadata = new PageMetadata(
      recipesPage.getContent.size,
      pageNumber + 1,
      recipesPage.getTotalElements,
      recipesPage.getTotalPages
    )
    val link = linkTo(
      methodOn(classOf[RecipeController]).listRecipes(
        Optional.of(pageNumber.toString),
        Optional.of(pageSize.toString),
        true
      )
    ).withSelfRel
      .andAffordance(
        afford(methodOn(classOf[RecipeController]).addRecipe(new Recipe, false))
      )
    val pagedModel =
      PagedModel.of(recipeCollectionModel.getContent, metadata, link)
    pagedModel
  }
}
