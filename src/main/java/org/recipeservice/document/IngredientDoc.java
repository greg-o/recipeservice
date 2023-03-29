package org.recipeservice.document;

import lombok.*;
import org.recipeservice.model.Ingredient;
import org.recipeservice.model.QuantitySpecifier;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IngredientDoc extends ElasticsearchDoc {

    private Long ingredientId;

    private int ingredientNumber;

    private QuantitySpecifier quantitySpecifier;

    private Double quantity;

    private String ingredient;

    public static IngredientDoc create(Ingredient ingredient) {
        return IngredientDoc.builder()
                .ingredientId(ingredient.ingredientId())
                .ingredientNumber(ingredient.ingredientNumber())
                .quantity(ingredient.quantity())
                .ingredient(ingredient.ingredient())
                .build();
    }
}
