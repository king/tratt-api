// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

public class StringEventValue extends Value {

    private final int index;
    private final String name;

    public StringEventValue(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toDebugString(Event e, Context context) {
        return String.format("[[source:event.%s]]'%s'", name, get(e, context));
    }

    @Override
    protected String getImp(Event e, Context context) {
        return ((TestEvent) e).getField(index);
    }

    @Override
    public String toString() {
        return "stringEvent[" + index + "]";
    }

}
