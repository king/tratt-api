package com.king.tratt;

import com.king.tratt.spi.Event;

@FunctionalInterface
public interface SimpleProcessor {

    void process(Event e);

}
