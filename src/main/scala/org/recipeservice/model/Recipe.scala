package org.recipeservice.model

import javax.persistence.{CascadeType, Column, Entity, FetchType, GeneratedValue, GenerationType, Id, JoinColumn, OneToMany, Table, Version}

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

  @OneToMany(targetEntity = classOf[Ingredient], mappedBy = "recipeId", cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  var ingredients: java.util.List[Ingredient] = _

  @OneToMany(targetEntity = classOf[Instruction], mappedBy = "recipeId", cascade=Array(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.LAZY)
  var instructions: java.util.List[Instruction] = _
}
