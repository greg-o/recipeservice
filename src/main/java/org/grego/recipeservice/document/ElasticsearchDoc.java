// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base Elasticsearch object.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ElasticsearchDoc {
    /**
     * The class of the Elasticsearch document.
     */
    @SuppressWarnings("checkstyle:membername")
    private String _class;
}
