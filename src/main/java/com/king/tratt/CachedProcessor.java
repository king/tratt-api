package com.king.tratt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.king.tratt.spi.Event;

class CachedProcessor<E extends Event> implements SimpleProcessor<E> {

    final BlockingQueue<E> blockingQueue = new LinkedBlockingQueue<>();

    @Override
    public void process(E e) {
        blockingQueue.add(e);
    }

    BlockingQueue<E> getQueue() {
        return blockingQueue;
    }

    @Override
    public String toString() {
        return blockingQueue.toString();
    }
}
