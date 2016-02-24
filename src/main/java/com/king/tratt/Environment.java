package com.king.tratt;

import java.util.HashMap;
import java.util.Map;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

class Environment<E extends Event> {

    final Map<String, Value<E>> localVariables = new HashMap<>();
    final Map<String, String> tdlVariables;

    public Environment(Map<String, String> tdlVariables) {
        this.tdlVariables = tdlVariables;
    }

    @Override
    public String toString() {
        return "Environment [localVariables=" + localVariables + ", tdlVariables=" + tdlVariables + "]";
    }

}
