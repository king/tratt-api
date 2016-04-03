package com.king.tratt;

import com.king.tratt.spi.Event;

public interface EventFilter {

    boolean accept(Event event);

}
