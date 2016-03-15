package com.king.tratt;

import java.util.HashMap;
import java.util.Map;

import com.king.tratt.spi.Context;

public final class ContextImp implements Context{

    private final Map<String, Object> map = new HashMap<>();

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    public Object get(String name) {
        return map.get(name);
    }

    void set(String name, Object value) {
        map.put(name, value);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
