package com.king.tratt;

import java.util.HashMap;
import java.util.Map;

import com.king.tratt.spi.Context;

class Environment {
    Context context;
    Map<String, String> variables = new HashMap<>();

    public Environment(Context context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Environment [context=" + context + ", variables=" + variables + "]";
    }

}
