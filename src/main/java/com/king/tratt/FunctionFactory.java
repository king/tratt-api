package com.king.tratt;


import java.util.List;

import com.king.tratt.spi.Value;

interface FunctionFactory {

    static final int VAR_ARG = -1;

    String getName();

    int getNumberOfArguments();

    Value create(List<Value> arguments);
}
