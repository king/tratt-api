package com.king.tratt;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.king.tratt.spi.Event;

@FunctionalInterface
interface PipelineProducerStrategy {

    static <E extends Event> PipelineProducerStrategy getDefault() {
        return (BlockingQueue<Event> q, Event e) -> q.add(e);
    }

    static <E extends Event> PipelineProducerStrategy getFiltered(final Set<Long> eventsToInclude) {
        return (BlockingQueue<Event> q, Event e) -> {
            if (eventsToInclude.contains(e.getId())) {
                q.add(e);
            }
        };
    }

    void apply(BlockingQueue<Event> q, Event e);

}
