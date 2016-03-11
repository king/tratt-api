package com.king.tratt;

import com.king.tratt.metadata.spi.Event;

@FunctionalInterface
public interface SimpleProcessor<E extends Event> {

    void process(E e);

}
