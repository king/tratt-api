package com.king.tratt.spi;

import java.util.Iterator;

/*
 * Interface that decouples the source of the Events, for
 * example: Kafka queue, or a pre-recorded dump file.
 */
public interface EventIterator<E extends Event> extends Iterator<E>, Stoppable {

    /**
     * This must be a blocking call. Shall only return false after {@link #stop()} method has been
     * called, or if thread is interrupted. Shall return true as soon as there is an next event.
     *
     * @return
     */
    @Override
    boolean hasNext();

    /**
     * @return the next event.
     */
    @Override
    E next();

    void start();

    @Override
    void stop();
}
