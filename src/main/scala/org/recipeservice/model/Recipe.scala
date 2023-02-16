package org.recipeservice.model

import java.time.LocalDateTime
import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, MapKey, OneToMany, Table, Version}
import javax.validation.constraints.NotNull


@Entity
@Table(name = "recipes")
class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "recipe_id")
  var recipeId: Long = _

  @Column(name = "name", length=256)
  @NotNull
  var name: String = _

  @Column(name = "description")
  @NotNull
  var description: String = _

  @Version
  @Column(name = "version")
  private val version = 0

  @Column(name = "creation_data_time", columnDefinition = "TIMESTAMP", nullable = false)
  var creationDateTime: LocalDateTime = _

  @Column(name = "last_modified_data_time", columnDefinition = "TIMESTAMP", nullable = false)
  var lastModifiedDateTime: LocalDateTime = _

  @OneToMany(targetEntity = classOf[Ingredient], cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  @MapKey
  var ingredients: java.util.List[Ingredient] = _

  @OneToMany(targetEntity = classOf[Instruction], cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  @MapKey
  var instructions: java.util.List[Instruction] = _
}
