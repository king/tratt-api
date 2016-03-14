package com.king.tratt.metadata.test.imp;

import com.king.tratt.metadata.spi.BooleanValue;
import com.king.tratt.metadata.spi.Context;


public class BooleanEventValue extends BooleanValue<TestEvent> {

    private int index;
    private String name;

    public BooleanEventValue(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.%s]]%s", name, get(e, context));
    }

    @Override
    public String toString() {
        return "booleanEvent[" + index + "]";
    }

    @Override
    protected Boolean _get(TestEvent e, Context context) {
        return Boolean.valueOf(e.getField(index));
    }

}
