package com.king.tratt;


@FunctionalInterface
public interface SimpleProcessor<E extends Event> {

    void process(E e);

}
