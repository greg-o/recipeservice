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
import org.grego.recipeservice.model.Ingredient;
import org.grego.recipeservice.model.QuantitySpecifier;

/**
 * IngredientDoc contains the ingredient information of a recipe for Elasticsearch.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IngredientDoc extends ElasticsearchDoc {

    /**
     * Identifier for the ingredient.
     */
    private Long ingredientId;

    /**
     * Ingredient number.
     */
    private int ingredientNumber;

    /**
     * Quantity specifier for the ingredient.
     */
    private QuantitySpecifier quantitySpecifier;

    /**
     * Quantity of the ingredient.
     */
    private Double quantity;

    /**
     * The ingredient.
     */
    private String ingredient;

    /**
     * Create IngredientDoc from Ingredient.
     * @param ingredient
     * @return IngredientDoc
     */
    public static IngredientDoc create(final Ingredient ingredient) {
        return IngredientDoc.builder()
                .ingredientId(ingredient.ingredientId())
                .ingredientNumber(ingredient.ingredientNumber())
                .quantity(ingredient.quantity())
                .ingredient(ingredient.ingredient())
                .build();
    }
}
