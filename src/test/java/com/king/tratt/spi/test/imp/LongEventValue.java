/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;


public class LongEventValue extends Value {

    private int index;
    private String name;

    public LongEventValue(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toDebugString(Event e, Context context) {
        return String.format("[[source:event.%s]]%s", name, get(e, context));
    }

    @Override
    protected Long getImp(Event e, Context context) {
        return Long.valueOf(((TestEvent) e).getField(index));
    }

    @Override
    public String toString() {
        return "longEvent[" + index + "]";
    }

}
