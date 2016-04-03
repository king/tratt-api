package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.king.tratt.spi.Stoppable;

public final class Multicaster {

    private final Preprocessor preprocessor;
    private final List<EventProcessorBuilder> builders;
    private List<StartedEventProcessor> startedProcessors;
    private final ExecutorService executor = util.newThreadPool();
    private final List<Stoppable> stoppables = new ArrayList<>();

    Multicaster(Preprocessor pre, List<EventProcessorBuilder> builders) {
        this.preprocessor = pre;
        this.builders = builders;
        stoppables.add(() -> executor.shutdownNow());
    }

    public StartedEventProcessor getStartedEventProcessor(int index) {
        return startedProcessors.get(index);
    }

    public boolean isCompleted() {
        return startedProcessors.stream().allMatch(s -> s.isCompleted());
    }

    public void awaitCompletion() {
        startedProcessors.parallelStream().forEach(s -> s.awaitCompletion());
        shutdown();
    }

    public void shutdown() {
        util.shutdownStoppablesAndExecutorService(stoppables, executor);
    }

    public List<StartedEventProcessor> getStartedEventProcessors() {
        return unmodifiableList(startedProcessors);
    }

    Multicaster start() {
        PipelineEventMulticaster multicaster = new PipelineEventMulticaster(preprocessor.eventCache.blockingQueue);
        startedProcessors = builders.parallelStream()
                .map(eventProcessorBuilder -> {
                    CachingProcessor cache = new CachingProcessor();
                    multicaster.addProcessor(cache);
                    return eventProcessorBuilder.setEventCache(cache).start();
                })
                .collect(toList());
        stoppables.add(() -> preprocessor.shutdown());
        startedProcessors.forEach(started -> stoppables.add(() -> started.shutdown()));
        executor.execute(multicaster);
        return this;
    }

}
