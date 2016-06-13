package com.king.tratt.spi.test.imp2;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

public class JsonValue extends Value {

    private String fieldName;

    public JsonValue(String node) {
        this.fieldName = node;
    }

    @Override
    public String toDebugString(Event e, Context context) {
        return String.format("[[source:event.%s]]'%s'", fieldName, get(e, context));
    }

    @Override
    protected Object getImp(Event e, Context context) {
        return ((JsonEvent) e).get(fieldName);
    }

    @Override
    public String toString() {
        return String.format("[[source:event.%s]]", fieldName);
    }

}
