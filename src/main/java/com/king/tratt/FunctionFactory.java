package com.king.tratt;


import java.util.List;

interface FunctionFactory<E extends Event> {

    static final int VAR_ARG = -1;

    String getName();

    int getNumberOfArguments();

    Value<E> create(List<Value<E>> arguments);
}
