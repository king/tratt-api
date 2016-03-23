package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Value;


public class TimeStampEventValue extends Value<TestEvent> {

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.timestamp]]%s", get(e, context));

    }

    @Override
    protected Long getImp(TestEvent e, Context context) {
        return e.getTimestampMillis();
    }

    @Override
    public String toString() {
        return "timestampEvent";
    }

}
