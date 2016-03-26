package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;


public class BooleanEventValue extends Value {

    private int index;
    private String name;

    public BooleanEventValue(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toDebugString(Event e, Context context) {
        return String.format("[[source:event.%s]]%s", name, get(e, context));
    }

    @Override
    public String toString() {
        return "booleanEvent[" + index + "]";
    }

    @Override
    protected Boolean getImp(Event e, Context context) {
        return Boolean.valueOf(((TestEvent) e).getField(index));
    }

}
