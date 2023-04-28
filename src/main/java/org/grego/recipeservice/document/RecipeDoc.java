// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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
@ToString
@Document(indexName = "recipes")
public class RecipeDoc extends ElasticsearchDoc {

    /**
     * All arguments constructor.
     * @param id
     * @param name
     * @param variation
     * @param description
     * @param creationDateTime
     * @param lastModifiedDateTime
     * @param ingredients
     * @param instructions
     */
    public RecipeDoc(final Long id, final String name, final int variation, final String description,
                     final Date creationDateTime, final Date lastModifiedDateTime,
                     final List<IngredientDoc> ingredients, final List<InstructionDoc> instructions) {
        this.id = id;
        this.name = name;
        this.variation = variation;
        this.description = description;
        this.creationDateTime = creationDateTime;
        this.lastModifiedDateTime = lastModifiedDateTime;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

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
    @Builder.Default
    private List<IngredientDoc> ingredients = Collections.emptyList();

    /**
     * The instructions for the recipe.
     */
    @Field(type = FieldType.Nested, includeInParent = true)
    @Builder.Default
    private List<InstructionDoc> instructions = Collections.emptyList();
}
