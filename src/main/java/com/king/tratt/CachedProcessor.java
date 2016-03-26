package com.king.tratt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.king.tratt.spi.Event;

class CachedProcessor implements SimpleProcessor {

    final BlockingQueue<Event> blockingQueue = new LinkedBlockingQueue<>();

    @Override
    public void process(Event e) {
        blockingQueue.add(e);
    }

    BlockingQueue<Event> getQueue() {
        return blockingQueue;
    }

    @Override
    public String toString() {
        return blockingQueue.toString();
    }
}
