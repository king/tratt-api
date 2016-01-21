package com.king.tratt.spi;


abstract class SequenceEventProcessor<E extends Event> implements EventProcessor<E> {

    abstract void onTimeout();

    abstract void beforeStart();

    void emit() {

    }
}
