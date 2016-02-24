package com.king.tratt;

import java.util.HashMap;
import java.util.Map;

import com.king.tratt.spi.Context;

class ContextImp implements Context {

    private final Map<String, String> map = new HashMap<>();

    @Override
    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    @Override
    public String get(String name) {
        return map.get(name);
    }

    void set(String name, String value) {
        map.put(name, value);
    }

    @Override
    public String toString() {
        return map.toString();
    }


}
