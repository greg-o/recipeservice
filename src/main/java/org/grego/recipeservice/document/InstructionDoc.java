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
import org.grego.recipeservice.model.Instruction;

/**
 * InstructionDoc contains the instruction information of a recipe for Elasticsearch.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstructionDoc extends ElasticsearchDoc {

    /**
     * Identifier for the instruction.
     */
    private Long instructionId;

    /**
     * Number of the instruction, used to order instructions.
     */
    private int instructionNumber;

    /**
     * The instruction.
     */
    private String instruction;

    /**
     * Create InstructionDoc from Instruction.
     * @param instruction
     * @return InstructionDoc
     */
    public static InstructionDoc create(final Instruction instruction) {
        return InstructionDoc.builder()
                .instructionId(instruction.instructionId())
                .instructionNumber(instruction.instructionNumber())
                .instruction(instruction.instruction())
                .build();
    }
}
