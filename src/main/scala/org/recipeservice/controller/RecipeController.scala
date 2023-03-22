package org.recipeservice.controller

import com.sun.tools.javac.util.DefinedBy.Api
import de.ingogriebsch.spring.hateoas.siren.MediaTypes

import java.util.Optional
import io.micrometer.core.annotation.Timed
import org.recipeservice.model.Recipe
import org.recipeservice.repository.RecipeRepository
import org.recipeservice.services.IRecipeService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.PagedModel.PageMetadata
import org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.{afford, linkTo}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.http.MediaType
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.{DeleteMapping, GetMapping, PatchMapping, PathVariable, PutMapping, RequestBody, RequestMapping, RequestParam, RestController}
import org.springframework.web.server.ResponseStatusException
import io.swagger.v3.oas.annotations.{OpenAPIDefinition, Operation}
import io.swagger.v3.oas.annotations.info.{Contact, Info}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.{ApiResponse, ApiResponses}

@RestController
@OpenAPIDefinition(
  info = Info(
    title = "Recipe Service APIs",
    version = "1.0.0",
    description = "Recipe REST APIs",
    contact = Contact(email="greg.osgood@gmail.com")
  )
)
@RequestMapping(path = Array("/recipes"), produces = Array(MediaTypes.SIREN_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE))
class RecipeController {

  @Autowired
  private var recipeService: IRecipeService = _

  @Autowired
  var recipeResourceAssembler: RecipeResourceAssembler = _

  @Value("${service.default_page_size:20}")
  private var defaultPageSize: String = _

  @Timed
  @Operation(
    summary = "Returns list of recipes",
    description = "This service returns a list of recipes",
    method = "listRecipes",
    responses = Array(
      ApiResponse(responseCode = "200", description = "The request has succeeded.", content = Array(Content(mediaType = MediaTypes.SIREN_JSON_VALUE, schema = Schema(implementation = classOf[Recipe])))),
      ApiResponse(responseCode = "500", description = "Internal server error.")
  ))
  @GetMapping(path = Array("/list"))
  def listRecipes(@RequestParam(name = "page-number") page: Optional[String], @RequestParam(name = "page-size") size: Optional[String], @RequestParam(name = "include-hyper-links", defaultValue = "false") includeHyperLinks: Boolean): ResponseEntity[?] = {
    val pageNumber = Integer.parseInt(page.orElse("0"))
    val pageSize = Integer.parseInt(size.orElse(defaultPageSize))
    val recipesPage = recipeService.getAllRecipes(pageNumber, pageSize)

    if (includeHyperLinks) {
      val recipeCollectionModel = recipeResourceAssembler.toCollectionModel(recipesPage.getContent())
      val metadata = new PageMetadata(recipesPage.getContent().size(),  pageNumber + 1, recipesPage.getTotalElements(), recipesPage.getTotalPages())
      val link = linkTo(methodOn(classOf[RecipeController]).listRecipes(Optional.of(pageNumber.toString()), Optional.of(pageSize.toString()), true)).withSelfRel()
        .andAffordance(afford(methodOn(classOf[RecipeController]).addRecipe(null, false)))
      val pagedModel = PagedModel.of(recipeCollectionModel.getContent(), metadata, link)
      ResponseEntity.ok().contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON).body(pagedModel)
    } else
      ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(recipesPage.getContent())
  }

  @Timed
  @Operation(
    summary = "Returns list a recipe",
    description = "This service returns a recipe for the given id",
    method = "getRecipe",
    responses = Array(
      ApiResponse(responseCode = "200", description = "The request has succeeded.", content = Array(Content(mediaType = MediaTypes.SIREN_JSON_VALUE, schema = Schema(implementation = classOf[Recipe])))),
      ApiResponse(responseCode = "500", description = "Internal server error.")
    ))
  @GetMapping(path = Array("/get/{id}"))
  def getRecipe(@PathVariable("id") id: Long, @RequestParam(name = "include-hyper-links", defaultValue = "false") includeHyperLinks: Boolean): ResponseEntity[?]  = {
    val recipe = recipeService.getRecipeById(id)

    if (recipe.isEmpty()) ResponseEntity.notFound().build()
    else {
      if (includeHyperLinks) ResponseEntity.ok().contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON).body(recipeResourceAssembler.toModel(recipe.get()))
      else ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(recipe.get())
    }
  }

  @Timed
  @Operation(
    summary = "Adds a recipe",
    description = "This service adds a recipe",
    method = "addRecipe",
    responses = Array(
      ApiResponse(responseCode = "200", description = "The request has succeeded.", content = Array(Content(mediaType = MediaTypes.SIREN_JSON_VALUE, schema = Schema(implementation = classOf[Recipe])))),
      ApiResponse(responseCode = "500", description = "Internal server error.")
    ))
  @PutMapping(path = Array("/add"))
  def addRecipe(@RequestBody() recipe: Recipe, @RequestParam(name = "include-hyper-links", defaultValue = "false") includeHyperLinks: Boolean): ResponseEntity[?] = {
    val addedRecipe = recipeService.addRecipe(recipe)
    if (includeHyperLinks) ResponseEntity.ok().contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON).body(recipeResourceAssembler.toModel(addedRecipe))
    else ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(addedRecipe)
  }

  @Timed
  @Operation(
    summary = "Updates a recipe",
    description = "This service updates a recipe",
    method = "updateRecipe",
    responses = Array(
      ApiResponse(responseCode = "200", description = "The request has succeeded."),
      ApiResponse(responseCode = "500", description = "Internal server error.")
    ))
  @PatchMapping(path = Array("/update"))
  def updateRecipe(@RequestBody() recipe: Recipe, @RequestParam(name = "include-hyper-links", defaultValue = "false") includeHyperLinks: Boolean): ResponseEntity[?] = ResponseEntity.ok(recipeResourceAssembler.toModel(recipeService.updateRecipe(recipe)))

  @Timed
  @Operation(
    summary = "Deletes a recipe",
    description = "This service deletes a recipe",
    method = "deleteRecipe",
    responses = Array(
      ApiResponse(responseCode = "200", description = "The request has succeeded."),
      ApiResponse(responseCode = "500", description = "Internal server error.")
    ))
  @DeleteMapping(path = Array("/delete/{id}"))
  def deleteRecipe(@PathVariable("id") id: Long): ResponseEntity[?] = {
    if (!recipeService.recipeExistsById(id)) ResponseEntity.notFound().build()
    else {
      recipeService.deleteRecipeById(id)
      ResponseEntity.ok("Delete recipe: id = " + id)
    }
  }
}
