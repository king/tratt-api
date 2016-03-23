package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Value;


public class BooleanEventValue extends Value<TestEvent> {

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
    protected Boolean getImp(TestEvent e, Context context) {
        return Boolean.valueOf(e.getField(index));
    }

}
