// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.grego.recipeservice.model.Recipe;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * RecipeDoc contains the recipe information for Elasticsearch.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "recipes")
public class RecipeDoc extends ElasticsearchDoc {
    /**
     * Identifier for the recipe.
     */
    @Id
    private Long id;

    /**
     * Name of the recipe.
     */
    private String name;

    /**
     * Variation of the recipe.
     */
    private int variation;

    /**
     * Description for the recipe.
     */
    private String description;

    /**
     * Date and time that the recipe was created.
     */
    private Date creationDateTime;

    /**
     * Date and time that the recipe was last modified.
     */
    private Date lastModifiedDateTime;

    /**
     * The ingredients of the recipe.
     */
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<IngredientDoc> ingredients = Collections.emptyList();

    /**
     * The instructions for the recipe.
     */
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<InstructionDoc> instructions = Collections.emptyList();

    /**
     * Create RecipeDoc from Recipe.
     * @param recipe
     * @return RecipeDoc
     */
    public static RecipeDoc create(final Recipe recipe) {
        return RecipeDoc.builder()
                .id(recipe.recipeId())
                .name(recipe.name())
                .variation(recipe.variation())
                .description(recipe.description())
                .creationDateTime(Date.from(recipe.creationDateTime().toInstant(ZoneOffset.UTC)))
                .lastModifiedDateTime(Date.from(recipe.lastModifiedDateTime().toInstant(ZoneOffset.UTC)))
                .ingredients(recipe.ingredients().stream().map(IngredientDoc::create).collect(Collectors.toList()))
                .instructions(recipe.instructions().stream().map(InstructionDoc::create).collect(Collectors.toList()))
                .build();
    }
}
