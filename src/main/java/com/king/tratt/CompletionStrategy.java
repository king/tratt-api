/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
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
