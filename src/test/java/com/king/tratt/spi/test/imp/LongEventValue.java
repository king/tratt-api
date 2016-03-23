package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Value;


public class LongEventValue extends Value<TestEvent> {

    private int index;
    private String name;

    public LongEventValue(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.%s]]%s", name, get(e, context));
    }

    @Override
    protected Long getImp(TestEvent e, Context context) {
        return Long.valueOf(e.getField(index));
    }

    @Override
    public String toString() {
        return "longEvent[" + index + "]";
    }

}
