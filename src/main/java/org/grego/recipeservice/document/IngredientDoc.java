// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.grego.recipeservice.model.QuantitySpecifier;

/**
 * IngredientDoc contains the ingredient information of a recipe for Elasticsearch.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class IngredientDoc extends ElasticsearchDoc {
    /**
     * All Arguments constructor.
     * @param ingredientId
     * @param ingredientNumber
     * @param quantitySpecifier
     * @param quantity
     * @param ingredient
     */
    public IngredientDoc(final Long ingredientId, final int ingredientNumber,
                         final QuantitySpecifier quantitySpecifier, final Double quantity, final String ingredient) {
        this.ingredientId = ingredientId;
        this.ingredientNumber = ingredientNumber;
        this.quantitySpecifier = quantitySpecifier;
        this.quantity = quantity;
        this.ingredient = ingredient;
    }

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
}
