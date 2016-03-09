package com.king.tratt.test.imp;

import com.king.tratt.Context;
import com.king.tratt.LongValue;


public class LongEventValue extends LongValue<TestEvent> {

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
    protected Long _get(TestEvent e, Context context) {
        return Long.valueOf(e.getField(index));
    }

}
