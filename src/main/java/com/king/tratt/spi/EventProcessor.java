package com.king.tratt.spi;


public interface EventProcessor<E extends Event> {

    void process(E e);

}
