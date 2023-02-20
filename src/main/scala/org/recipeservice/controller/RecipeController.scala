package org.recipeservice.controller

import de.ingogriebsch.spring.hateoas.siren.MediaTypes
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

import java.util.Optional

@RestController
@RequestMapping(path = Array("/recipes"), produces = Array(MediaTypes.SIREN_JSON_VALUE))
class RecipeController {

  @Autowired
  private var recipeService: IRecipeService = _

  @Autowired
  var recipeResourceAssembler: RecipeResourceAssembler = _

  @Value("${service.default_page_size:20}")
  private var defaultPageSize: String = _

  @Timed
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
  @PutMapping(path = Array("/add"))
  def addRecipe(@RequestBody() recipe: Recipe, @RequestParam(name = "include-hyper-links", defaultValue = "false") includeHyperLinks: Boolean): ResponseEntity[?] = {
    val addedRecipe = recipeService.addRecipe(recipe)
    if (includeHyperLinks) ResponseEntity.ok().contentType(de.ingogriebsch.spring.hateoas.siren.MediaTypes.SIREN_JSON).body(recipeResourceAssembler.toModel(addedRecipe))
    else ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(addedRecipe)
  }

  @Timed
  @PatchMapping(path = Array("/update"))
  def updateRecipe(@RequestBody() recipe: Recipe, @RequestParam(name = "include-hyper-links", defaultValue = "false") includeHyperLinks: Boolean): ResponseEntity[?] = ResponseEntity.ok(recipeResourceAssembler.toModel(recipeService.updateRecipe(recipe)))

  @Timed
  @DeleteMapping(path = Array("/delete/{id}"))
  def deleteRecipe(@PathVariable("id") id: Long): ResponseEntity[?] = {
    if (!recipeService.recipeExistsById(id)) ResponseEntity.notFound().build()
    else ResponseEntity.ok(recipeResourceAssembler.toModel(recipeService.getRecipeById(id).get()))
  }
}
