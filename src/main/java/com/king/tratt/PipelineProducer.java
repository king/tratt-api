package com.king.tratt;

import static java.lang.Thread.currentThread;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PipelineProducer<E extends Event> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineProducer.class);
    private volatile EventIterator<E> iterator;
    private final BlockingQueue<E> blockingQueue;
    private final PipelineProducerStrategy<E> queueProducerStrategy;

    PipelineProducer(EventIterator<E> iterator, BlockingQueue<E> blockingQueue,
            PipelineProducerStrategy<E> queueProducerStrategy) {
        this.iterator = iterator;
        this.queueProducerStrategy = queueProducerStrategy;
        this.blockingQueue = blockingQueue;
    }

    //    @Override
    //    public void start() {
    //        iterator.start();
    //        future = executor.submit(new Runnable() {
    //
    //            @Override
    //            public void run() {
    //                while (iterator.hasNext()) {
    //                    E e = iterator.next();
    //                    queueProducerStrategy.apply(blockingQueue, e);
    //                }
    //            }
    //        });
    //    }

    //    @Override
    //    public final void stop() {
    //        iterator.stop(); // this triggers the iterator.hasNext() method to return false.
    //    }

    @Override
    public void run() {
        try {
            while (iterator.hasNext() && !currentThread().isInterrupted()) {
                E e = iterator.next();
                queueProducerStrategy.apply(blockingQueue, e);
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

}
