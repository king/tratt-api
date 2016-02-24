package com.king.tratt;

import static com.king.tratt.TrattUtil.format;

import java.util.List;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.StringValue;
import com.king.tratt.spi.Value;

class FunctionFactorySplit<E extends Event> implements FunctionFactory<E> {

    private Value<E> strValue;
    private Value<E> delimiterValue;
    private Value<E> indexValue;

    @Override
    public String getName() {
        return "split";
    }

    @Override
    public int getNumberOfArguments() {
        return 3;
    }

    @Override
    public Value<E> create(List<Value<E>> arguments) {
        strValue = arguments.get(0);
        delimiterValue = arguments.get(1);
        indexValue = arguments.get(2);
        return new StringValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return format(e, context, "[[source:split('~g', '~g', ~g)]]'~g'",
                        strValue, delimiterValue, indexValue, this);
            }

            @Override
            protected String _get(E e, Context context) {
                String str = strValue.asString(e, context);
                String delimiter = delimiterValue.asString(e, context);
                String[] strs = str.split(delimiter);
                int i = ((Long) indexValue.get(e, context)).intValue();
                if (i < strs.length) {
                    return strs[i];
                } else {
                    return String.format("[@ERROR array index '%s' out of bounce]", i);
                }
            }
        };
    }

}
