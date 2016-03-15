package com.king.tratt;

import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.king.tratt.spi.Context;

final class ContextImp implements Context {

    private final Map<String, Object> map = new HashMap<>();

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    public Object get(String name) {
        return map.get(name);
    }

    public void set(String name, Object value) {
        map.put(name, value);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return unmodifiableSet(map.entrySet());
    }
}
