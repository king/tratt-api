package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Value;


public class IdEventValue extends Value<TestEvent> {

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.id]]%s", get(e, context));
    }

    @Override
    protected Long getImp(TestEvent e, Context context) {
        return e.getId();
    }

    @Override
    public String toString() {
        return "event[id]";
    }

}
