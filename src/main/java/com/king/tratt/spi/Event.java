package com.king.tratt.spi;

/**
 * Must be Thread Safe.
 *
 */
public interface Event {
    long getId();
    long getTimestampMillis();
}
