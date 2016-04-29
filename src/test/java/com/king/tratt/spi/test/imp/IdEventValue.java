/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

public class IdEventValue extends Value {

    @Override
    public String toDebugString(Event e, Context context) {
        return String.format("[[source:event.id]]%s", get(e, context));
    }

    @Override
    protected String getImp(Event e, Context context) {
        return e.getId();
    }

    @Override
    public String toString() {
        return "event[id]";
    }

}
