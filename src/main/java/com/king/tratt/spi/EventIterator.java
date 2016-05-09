/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

import java.util.Iterator;

import com.king.tratt.EventProcessorBuilder;
import com.king.tratt.StartedEventProcessor;

/**
 * Interface that decouples {@code tratt-api} from the source of the Events.
 * Clients are expected to implement this to provide {@code tratt-api} with
 * {@link Event} instances.
 * <p>
 * NOTE!</br>
 * The {@link #stop()} method will be called from a separate thread.
 * Implementation needs to cope with this.
 */
public interface EventIterator extends Iterator<Event>, Stoppable {

    /**
     * This method shall block the current thread until there is a next
     * {@link Event} to consume by {@link EventIterator#next()} method. Shall
     * return true when the next event is ready to be consumed.
     * <p>
     * Shall only return false after {@link #stop()} method has been called, or
     * if current thread is interrupted.
     * <p>
     * NOTE!</br>
     * The {@link #stop()} method is called from another thread. If this method
     * is blocking while {@link #stop()} method is called, this method should
     * release and return {@code false}.
     *
     * @return true when there is a new event to consume, or false when the
     *         {@link EventIterator} has been stopped or thread is interrupted.
     */
    @Override
    boolean hasNext();

    /**
     * Should return an {@link Event} instance that basically wraps the client
     * event, in order to fulfill the contract specified by {@link Event}.
     *
     * @return the next {@link Event}.
     */
    @Override
    Event next();

    /**
     * Any initialization that is needed to consume the events should be done in
     * here. For example: Opening a connection to a server.
     * <p>
     * This method is called once by the {@code tratt-api} at the very beginning
     * of the execution life cycle. That is, when client calls
     * {@link EventProcessorBuilder#start()}.
     */
    void start();

    /**
     * This method is called once by the {@code tratt-api} at the end of
     * execution life cycle. That is, either when client calls
     * {@link StartedEventProcessor#shutdown()}, or when either one of these two
     * methods release:
     * <ol>
     * <li>{@link StartedEventProcessor#awaitCompletion()}</li>
     * <li>{@link StartedEventProcessor#awaitSuccess()}</li>
     * </ol>
     * <p>
     * NOTE!</br>
     * This method will be called from a separate thread. Implementation needs
     * to cope with this.
     */
    @Override
    void stop();
}
