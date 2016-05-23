// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

@FunctionalInterface
public interface CompletionStrategy extends SequenceProcessorListener {

    /**
     * Optional to implement
     *
     * @param processors
     */
    default void beforeStart(Processors processors) {
    }

    boolean isCompleted();

}
