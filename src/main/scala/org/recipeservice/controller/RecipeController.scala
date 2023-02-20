package org.recipeservice.controller

import de.ingogriebsch.spring.hateoas.siren.MediaTypes
import io.micrometer.core.annotation.Timed
import org.recipeservice.model.Recipe
import org.recipeservice.repository.RecipeRepository
import org.recipeservice.services.IRecipeService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
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
  def list(@RequestParam start: Optional[String], @RequestParam limit: Optional[String]): ResponseEntity[?] = {
    val recipes = recipeService.getAllRecipes(Integer.parseInt(start.orElse("0")), Integer.parseInt(limit.orElse(defaultPageSize)))
    ResponseEntity.ok(recipeResourceAssembler.toCollectionModel(recipes))
  }

  @Timed
  @GetMapping(path = Array("/get/{id}"))
  def get(@PathVariable("id") id: Long): ResponseEntity[?]  = {
    val recipe = recipeService.getRecipeById(id)

    if (recipe.isEmpty()) ResponseEntity.notFound().build()
    else ResponseEntity.ok(recipeResourceAssembler.toModel(recipe.get()))
  }


  @Timed
  @PutMapping(path = Array("/add"))
  def addRecipe(@RequestBody() recipe: Recipe): ResponseEntity[?] = ResponseEntity.ok(recipeResourceAssembler.toModel(recipeService.addRecipe(recipe)))

  @Timed
  @PatchMapping(path = Array("/update"))
  def updateRecipe(@RequestBody() recipe: Recipe): ResponseEntity[?] = ResponseEntity.ok(recipeResourceAssembler.toModel(recipeService.updateRecipe(recipe)))

  @Timed
  @DeleteMapping(path = Array("/delete/{id}"))
  def deleteById(@PathVariable("id") id: Long): ResponseEntity[?] = {
    if (!recipeService.recipeExistsById(id)) ResponseEntity.notFound().build()
    else ResponseEntity.ok(recipeResourceAssembler.toModel(recipeService.getRecipeById(id).get()))
  }
}
