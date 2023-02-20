package org.recipeservice.controller

import org.recipeservice.model.Recipe
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.{CollectionModel, EntityModel, LinkRelation}
import org.springframework.stereotype.Component
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler
import org.springframework.hateoas.server.core.DummyInvocationUtils.methodOn
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.{afford, linkTo}
import org.springframework.http.HttpMethod

import java.util.Optional

@Component
class RecipeResourceAssembler extends SimpleRepresentationModelAssembler[Recipe] {

  override def addLinks(resource: EntityModel[Recipe]): Unit = {

    resource.add(linkTo(methodOn(classOf[RecipeController]).getRecipe(resource.getContent().recipeId, false)).withSelfRel()
      .andAffordance(afford(methodOn(classOf[RecipeController]).deleteRecipe(resource.getContent().recipeId)))
      .andAffordance(afford(methodOn(classOf[RecipeController]).updateRecipe(resource.getContent(), false)))
    )
  }

  override def addLinks(resources: CollectionModel[EntityModel[Recipe]]): Unit = {}
}
