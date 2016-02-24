package com.king.tratt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.SimpleProcessor;

final class PipelineConsumer<E extends Event> implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineConsumer.class);
    private final BlockingQueue<E> blockingQueue;
    private final CopyOnWriteArrayList<SimpleProcessor<E>> processors = new CopyOnWriteArrayList<>();


    PipelineConsumer(BlockingQueue<E> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    //    void start() {
    //        executor.submit(new Runnable() {
    //            @Override
    //            public void run() {
    //                try {
    //                    while (!Thread.currentThread().isInterrupted()) {
    //                        E e = blockingQueue.take();
    //                        for (Processor<E> processor : processors) {
    //                            processor.process(e);
    //                        }
    //                    }
    //                } catch (InterruptedException e) {
    //                    // exit thread gracefully.
    //                } catch (Exception e) {
    //                    String message = "Unexpected crash when consuming the eventQueue.";
    //                    throw new IllegalStateException(message, e);
    //                }
    //            }
    //        });
    //    }

    void addProcessor(SimpleProcessor<E> processor) {
        processors.add(processor);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                E e = blockingQueue.take();
                for (SimpleProcessor<E> processor : processors) {
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
