package org.recipeservice.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.hateoas.RepresentationModel

import java.time.LocalDateTime
import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, MapKey, OneToMany, Table, UniqueConstraint, Version}
import javax.validation.constraints.NotNull

@Entity
@Schema(name = "Recipe")
@Table(name = "recipes", uniqueConstraints = Array(UniqueConstraint(name = "unique_recipe_name_and_variation", columnNames = Array("name", "variation"))))
class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recipe_id")
  @Schema(description = "The generated ID when saved in database", name = "recipeId")
  var recipeId: Long = _

  @Column(name = "name", length=256)
  @NotNull
  @Schema(description = "The name of the recipe", name = "name")
  var name: String = _

  @Column(name = "variation", nullable = false)
  @Schema(description = "The variation of the recipe", name = "variation")
  var variation: Int = _

  @Column(name = "description")
  @NotNull
  @Schema(description = "The description of the recipe", name = "description")
  var description: String = _

  @Version
  @Column(name = "version")
  @Schema(description = "The version of the recipe", name = "version")
  private val version: Long = 0

  @Column(name = "creation_date_time", columnDefinition = "TIMESTAMP", nullable = false)
  @Schema(description = "The date and time the recipe was created", name = "creationDateTime")
  var creationDateTime: LocalDateTime = _

  @Column(name = "last_modified_date_time", columnDefinition = "TIMESTAMP", nullable = false)
  @Schema(description = "The date and time the recipe was last modified", name = "lastModifiedDateTime")
  var lastModifiedDateTime: LocalDateTime = _

  @OneToMany(targetEntity = classOf[Ingredient], cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  @MapKey
  @Schema(description = "The ingredients of the recipe", name = "ingredients")
  var ingredients: java.util.List[Ingredient] = _

  @OneToMany(targetEntity = classOf[Instruction], cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  @MapKey
  @Schema(description = "The instructions for the recipe", name = "instructions")
  var instructions: java.util.List[Instruction] = _
}
