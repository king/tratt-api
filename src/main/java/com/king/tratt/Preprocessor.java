/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.Tratt.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.Stoppable;

public final class Preprocessor {
    CachingProcessor eventCache;
    private final ExecutorService executor = util.newThreadPool();
    private final List<Stoppable> stoppables;
    private final BlockingQueue<Event> pipeline;
    private final List<EventIterator> eventIterators;
    private final ArrayList<SimpleProcessor> simpleProcessors;

    Preprocessor(PreprocessorBuilder builder) {
        // first copy...
        pipeline = builder.pipeline; // don't copy!
        // statisticsDataHolder =
        // StatisticsDataHolder.copyOf(builder.statisticsDataHolder);
        eventIterators = new ArrayList<>(builder.eventIterators);
        simpleProcessors = new ArrayList<>(builder.simpleProcessors);
        stoppables = new ArrayList<>(builder.stoppables);
        // ...then check invariants
        checkEventIteratorsNotEmpty();

        stoppables.add(() -> executor.shutdownNow());
    }

    public Preprocessor start() {
        eventCache = util.startProcessingEventsAndCreateCache(pipeline, eventIterators, stoppables,
                simpleProcessors, executor);
        return this;
    }

    private void checkEventIteratorsNotEmpty() { // TODO move to baseClass or
                                                 // Util?
        if (eventIterators.isEmpty()) {
            String message = "No EventIterators added!";
            throw new IllegalStateException(message);
        }
    }

    public void shutdown() {
        util.shutdownStoppablesAndExecutorService(stoppables, executor);
    }

}
