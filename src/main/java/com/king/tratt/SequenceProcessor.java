package com.king.tratt;

import java.util.List;

import com.king.tratt.spi.Event;

public abstract class SequenceProcessor<E extends Event> {

    public abstract void beforeStart();

    public abstract void onTimeout();

    public abstract void process(E e);

    void initiate() {

    }

    String getName() {
        return null;
    }

    protected void emit() {

    }

    void setCheckPointMatchers(List<CheckPointMatcher<E>> cpMatchers) {
        // TODO Auto-generated method stub

    }

    void setListeners() {
        // TODO Auto-generated method stub

    }
}
