package com.king.tratt.test.imp;

import com.king.tratt.metadata.spi.Context;
import com.king.tratt.metadata.spi.LongValue;


public class IdEventValue extends LongValue<TestEvent> {

    @Override
    public String toDebugString(TestEvent e, Context context) {
        return String.format("[[source:event.id]]%s", get(e, context));
    }

    @Override
    protected Long _get(TestEvent e, Context context) {
        return e.getId();
    }

}
