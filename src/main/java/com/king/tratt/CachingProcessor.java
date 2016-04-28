/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.king.tratt.spi.Event;

class CachingProcessor implements SimpleProcessor {

    final BlockingQueue<Event> blockingQueue = new LinkedBlockingQueue<>();

    @Override
    public void process(Event e) {
        blockingQueue.add(e);
    }

    @Override
    public String toString() {
        return "eventCache: " + blockingQueue.toString();
    }

    Event poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return blockingQueue.poll(timeout, timeUnit);
    }
}
