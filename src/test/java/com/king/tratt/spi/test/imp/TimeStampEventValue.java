/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;


public class TimeStampEventValue extends Value {

    @Override
    public String toDebugString(Event e, Context context) {
        return String.format("[[source:event.timestamp]]%s", get(e, context));

    }

    @Override
    protected Long getImp(Event e, Context context) {
        return e.getTimestampMillis();
    }

    @Override
    public String toString() {
        return "timestampEvent";
    }

}
