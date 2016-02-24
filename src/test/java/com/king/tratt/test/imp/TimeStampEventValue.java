package com.king.tratt.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.LongValue;


public class TimeStampEventValue extends LongValue<TestEvent> {

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.timestamp]]%s", get(e, context));

    }

    @Override
    protected Long _get(TestEvent e, Context context) {
        return e.getTimestamp();
    }

}
