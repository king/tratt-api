package com.king.tratt;

import com.king.tratt.spi.Event;

@FunctionalInterface
public interface CompletionStrategy<E extends Event> extends SequenceProcessorListener<E> {

    default void beforeStart(Processors processors) {
    }

    boolean isCompleted();

}
