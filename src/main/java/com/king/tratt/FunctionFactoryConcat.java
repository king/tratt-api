package com.king.tratt;

import static com.king.tratt.TrattUtil.format;
import static com.king.tratt.TrattUtil.formatJoin;

import java.util.List;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.StringValue;
import com.king.tratt.spi.Value;

class FunctionFactoryConcat<E extends Event> implements FunctionFactory<E> {

    @Override
    public String getName() {
        return "concat";
    }

    @Override
    public int getNumberOfArguments() {
        return VAR_ARG;
    }

    @Override
    public Value<E> create(List<Value<E>> arguments) {
        return new StringValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                String joinedValues = formatJoin(e, context, ", ", "'~g'", arguments);
                return format(e, context, "[[source:concat(~s)]]'~g'", joinedValues, this);
            }

            @Override
            protected String _get(E e, Context context) {
                StringBuilder s = new StringBuilder();
                for (Value<E> value : arguments) {
                    s.append(value.asString(e, context));
                }
                return s.toString();
            }
        };
    }

}
