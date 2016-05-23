// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import java.util.List;

import com.king.tratt.spi.Value;

interface FunctionFactory {

    static final int VAR_ARG = -1;

    String getName();

    int getNumberOfArguments();

    Value create(List<Value> arguments);
}
