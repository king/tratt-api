package com.king.tratt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FunctionFactoryProvider<E extends Event> {

    private Map<String, FunctionFactory<E>> functions = new HashMap<>();

    FunctionFactoryProvider() {
        addFunction(new FunctionFactoryJsonField<>());
        addFunction(new FunctionFactorySubstr<>());
        addFunction(new FunctionFactoryConcat<>());
        addFunction(new FunctionFactorySplit<>());

    }
    private void addFunction(FunctionFactory<E> func) {
        functions.put(func.getName(), func);
    }

    FunctionFactory<E> get(String name) {
        return functions.get(name);
    }

    List<String> getFunctionNames() {
        return new ArrayList<>(functions.keySet());
    }
}
