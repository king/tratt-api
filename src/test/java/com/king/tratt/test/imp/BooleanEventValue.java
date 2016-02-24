package com.king.tratt.test.imp;

import com.king.tratt.spi.BooleanValue;
import com.king.tratt.spi.Context;


public class BooleanEventValue extends BooleanValue<TestEvent> {

    private int index;
    private String name;

    public BooleanEventValue(int index, String name) {
        this.index = index;
        // TODO Auto-generated constructor stub
        this.name = name;
    }

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.%s]]%", name, get(e, context));
    }

    @Override
    protected Boolean _get(TestEvent e, Context context) {
        return Boolean.valueOf(e.getField(index));
    }

}
