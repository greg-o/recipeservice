package org.recipeservice.model

import java.time.LocalDateTime
import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, MapKey, OneToMany, Table, UniqueConstraint, Version}
import javax.validation.constraints.NotNull

@Entity
@Table(name = "recipes", uniqueConstraints = Array(UniqueConstraint(name = "unique_recipe_name_and_variation", columnNames = Array("name", "variation"))))
class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recipe_id")
  var recipeId: Long = _

  @Column(name = "name", length=256)
  @NotNull
  var name: String = _

  @Column(name = "variation", nullable = false)
  var variation: Int = _

  @Column(name = "description")
  @NotNull
  var description: String = _

  @Version
  @Column(name = "version")
  private val version = 0

  @Column(name = "creation_date_time", columnDefinition = "TIMESTAMP", nullable = false)
  var creationDateTime: LocalDateTime = _

  @Column(name = "last_modified_date_time", columnDefinition = "TIMESTAMP", nullable = false)
  var lastModifiedDateTime: LocalDateTime = _

  @OneToMany(targetEntity = classOf[Ingredient], cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  @MapKey
  var ingredients: java.util.List[Ingredient] = _

  @OneToMany(targetEntity = classOf[Instruction], cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  @MapKey
  var instructions: java.util.List[Instruction] = _
}
