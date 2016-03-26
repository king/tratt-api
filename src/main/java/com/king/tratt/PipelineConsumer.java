package com.king.tratt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.spi.Event;

final class PipelineConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineConsumer.class);
    private final BlockingQueue<Event> blockingQueue;
    private final CopyOnWriteArrayList<SimpleProcessor> processors = new CopyOnWriteArrayList<>();

    PipelineConsumer(BlockingQueue<Event> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    void addProcessor(SimpleProcessor processor) {
        processors.add(processor);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Event e = blockingQueue.take();
                for (SimpleProcessor processor : processors) {
                    processor.process(e);
                }
            }
        } catch (InterruptedException e) {
            // exit thread gracefully.
        } catch (Throwable e) {
            String message = "Unexpected crash when consuming the eventQueue.";
            LOG.error(message, e);
        }
    }

}
