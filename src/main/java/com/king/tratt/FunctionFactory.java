package com.king.tratt;


import java.util.List;

import com.king.tratt.metadata.spi.Event;
import com.king.tratt.metadata.spi.Value;

interface FunctionFactory<E extends Event> {

    static final int VAR_ARG = -1;

    String getName();

    int getNumberOfArguments();

    Value<E> create(List<Value<E>> arguments);
}
