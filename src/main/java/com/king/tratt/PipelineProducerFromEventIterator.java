package com.king.tratt;

import static java.lang.Thread.currentThread;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;

class PipelineProducerFromEventIterator implements Runnable {

    private volatile EventIterator iterator;
    private final BlockingQueue<Event> blockingQueue;

    PipelineProducerFromEventIterator(EventIterator iterator, BlockingQueue<Event> blockingQueue) {
        this.iterator = iterator;
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
            while (iterator.hasNext() && !currentThread().isInterrupted()) {
                blockingQueue.add(iterator.next());
            }
    }

}
