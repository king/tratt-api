package com.king.tratt;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.king.tratt.spi.Event;

abstract class PipelineProducerStrategy<E extends Event> {

    static <E extends Event> PipelineProducerStrategy<E> getDefault() {
        return new PipelineProducerStrategy<E>() {

            @Override
            void apply(BlockingQueue<E> q, E e) {
                q.add(e);
            }
        };
    }

    static <E extends Event> PipelineProducerStrategy<E> getFiltered(final Set<Long> eventsToInclude) {
        return new PipelineProducerStrategy<E>() {

            @Override
            void apply(BlockingQueue<E> q, E e) {
                if (eventsToInclude.contains(e.getId())) {
                    q.add(e);
                }
            }
        };
    }

    private PipelineProducerStrategy() {
        /* for private usage only */
    }
    abstract void apply(BlockingQueue<E> q, E e);

}
