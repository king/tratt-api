/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

/**
 * Must be Thread Safe.
 *
 */
public interface Event {
    String getId();

    long getTimestampMillis();
}
