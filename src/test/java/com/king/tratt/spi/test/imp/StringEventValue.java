package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Value;

public class StringEventValue extends Value<TestEvent> {

    private final int index;
    private final String name;

    public StringEventValue(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.%s]]'%s'", name, get(e, context));
    }

    @Override
    protected String getImp(TestEvent e, Context context) {
        return e.getField(index);
    }

    @Override
    public String toString() {
        return "stringEvent[" + index + "]";
    }

}
