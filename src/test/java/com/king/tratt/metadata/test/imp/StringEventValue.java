package com.king.tratt.metadata.test.imp;

import com.king.tratt.metadata.spi.Context;
import com.king.tratt.metadata.spi.StringValue;

public class StringEventValue extends StringValue<TestEvent> {

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
    protected String _get(TestEvent e, Context context) {
        return e.getField(index);
    }

    @Override
    public String toString() {
        return "stringEvent[" + index + "]";
    }

}
