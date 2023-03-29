package org.recipeservice.document;

import lombok.*;
import org.recipeservice.model.Ingredient;
import org.recipeservice.model.Instruction;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstructionDoc extends ElasticsearchDoc {

    private Long instructionId;

    private int instructionNumber;

    private String instruction;

    public static InstructionDoc create(Instruction instruction) {
        return InstructionDoc.builder()
                .instructionId(instruction.instructionId())
                .instructionNumber(instruction.instructionNumber())
                .instruction(instruction.instruction())
                .build();
    }
}
