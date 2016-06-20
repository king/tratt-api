// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static java.lang.Thread.currentThread;

import java.util.concurrent.BlockingQueue;

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
        try {
            while (iterator.hasNext() && !currentThread().isInterrupted()) {
                blockingQueue.add(iterator.next());
            }
        } catch (InterruptedException e) {
            // gracefully exit thread.
        }
    }

}
