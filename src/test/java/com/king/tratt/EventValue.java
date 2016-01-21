package com.king.tratt;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.DynamicValue;
import com.king.tratt.spi.Value;



public class EventValue extends DynamicValue<MyEvent> {

    private int index;

    public EventValue(String eventName, int index) {
        this.index = index;

    }

    @Override
    public String toDebugString(MyEvent e, Context context) {
        return String.format("[[source:%s.", index);
    }

    @Override
    protected Value<MyEvent> _get(MyEvent e, Context context) {

        return Values.constant(e.getField(index));
    }

}
