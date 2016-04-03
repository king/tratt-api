package com.king.tratt;

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.king.tratt.spi.Event;

final class PipelineEventMulticaster implements Runnable {
    private final BlockingQueue<Event> blockingQueue;
    /*
     * simpleProcessors can be populated from different threads, so ArrayList
     * needs to be synchronized.
     */
    private final List<SimpleProcessor> simpleProcessors = synchronizedList(new ArrayList<>());

    PipelineEventMulticaster(BlockingQueue<Event> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public String toString() {
        return simpleProcessors.toString();
    }

    PipelineEventMulticaster addProcessor(SimpleProcessor processor) {
        simpleProcessors.add(processor);
        return this;
    }

    PipelineEventMulticaster addProcessors(List<SimpleProcessor> processors) {
        simpleProcessors.addAll(processors);
        return this;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Event event = blockingQueue.take();
                simpleProcessors.parallelStream().forEach(processor -> {
                    processor.process(event);
                });
            }
        } catch (InterruptedException e) {
            // exit thread gracefully.
        }
    }

}
