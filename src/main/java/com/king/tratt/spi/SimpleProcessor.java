package com.king.tratt.spi;


@FunctionalInterface
public interface SimpleProcessor<E extends Event> {

    void process(E e);

}
