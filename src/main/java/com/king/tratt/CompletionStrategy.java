package com.king.tratt;

@FunctionalInterface
public interface CompletionStrategy<E extends Event> extends SequenceProcessorListener<E> {

    default void beforeStart(Processors processors) {
    }

    boolean isCompleted();

}
