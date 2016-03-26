package com.king.tratt.spi;


public interface Event {
    long getId();
    long getTimestampMillis();
    // TODO Maybe? Object getField(String)
}
