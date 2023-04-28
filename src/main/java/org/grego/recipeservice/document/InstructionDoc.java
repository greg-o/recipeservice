// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * InstructionDoc contains the instruction information of a recipe for Elasticsearch.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@ToString
public class InstructionDoc extends ElasticsearchDoc {
    /**
     * All arguments constructor.
     * @param instructionId
     * @param instructionNumber
     * @param instruction
     */
    public InstructionDoc(final Long instructionId, final int instructionNumber, final String instruction) {
        this.instructionId = instructionId;
        this.instructionNumber = instructionNumber;
        this.instruction = instruction;
    }

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
}
