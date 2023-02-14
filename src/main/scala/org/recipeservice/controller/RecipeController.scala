package org.recipeservice.controller

import io.micrometer.core.annotation.Timed
import org.recipeservice.model.Recipe
import org.recipeservice.repository.RecipeRepository
import org.recipeservice.services.IRecipeService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.{DeleteMapping, GetMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, RestController}
import org.springframework.web.server.ResponseStatusException

import java.util.Optional

@RestController
@RequestMapping(path = Array("/recipes"), produces = Array(APPLICATION_JSON_VALUE))
class RecipeController {

  @Autowired
  private var recipeService: IRecipeService = _

  @Value("${service.default_page_size}")
  private var defaultPageSize: String = _

  @Timed
  @GetMapping(path = Array("/list"))
  def list(model: Model, @RequestParam start: Optional[String], @RequestParam limit: Optional[String]): java.util.List[Recipe] = {

    val recipes = recipeService.getAllRecipes(Integer.parseInt(start.orElse("0")), Integer.parseInt(limit.orElse(defaultPageSize)))
    model.addAttribute("recipes", recipes)
    recipes
  }

  @Timed
  @GetMapping(path = Array("/get/{id}"))
  def get(@PathVariable("id") id: Long): Recipe = recipeService.getRecipeById(id).orElseThrow(() => new ResponseStatusException(HttpStatus.NOT_FOUND))


  @Timed
  @PostMapping(path = Array("/add"))
  def addRecipe(@RequestBody() recipe: Recipe): Recipe = recipeService.addRecipe(recipe)

  @Timed
  @PostMapping(path = Array("/update"))
  def updateRecipe(@RequestBody() recipe: Recipe): Recipe = recipeService.addRecipe(recipe)

  @Timed
  @DeleteMapping(path = Array("/delete/{id}"))
  def deleteById(@PathVariable("id") id: Long) = {
    if (!recipeService.recipeExistsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND)

    recipeService.deleteRecipeById(id)
  }
}
