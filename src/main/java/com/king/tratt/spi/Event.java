/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

/**
 * Implementations of this should wrap a client event and provide the {@code id}
 * and {@code timestamp} to the {@code tratt-api}.
 * <p>
 * Must be Thread Safe, preferable immutable.
 *
 */
public interface Event {
    /**
     * Returns the identifier of this particular type of event.
     * <p>
     * If no specific id exists, then the event name shall be returned.
     *
     * @return the identifier of this event.
     *
     * @see EventMetaDataFactory
     */
    String getId();

    /**
     * Returns the time when this event occurred.
     *
     * @return timstamp in milliseconds.
     */
    long getTimestampMillis();
}
