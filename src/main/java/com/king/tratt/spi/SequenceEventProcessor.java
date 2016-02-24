package com.king.tratt.spi;


abstract class SequenceEventProcessor<E extends Event> implements SimpleProcessor<E> {

    abstract void onTimeout();

    abstract void beforeStart();

    void emit() {

    }
}
