package com.king.tratt;

import static com.king.tratt.TrattUtil.format;

import java.util.List;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.StringValue;
import com.king.tratt.spi.Value;

class FunctionFactorySubstr<E extends Event> implements FunctionFactory<E> {

    Value<E> fromValue;
    Value<E> toValue;
    Value<E> strValue;

    @Override
    public String getName() {
        return "substr";
    }

    @Override
    public int getNumberOfArguments() {
        return 3;
    }

    @Override
    public Value<E> create(List<Value<E>> args) {
        fromValue = args.get(0);
        toValue = args.get(1);
        strValue = args.get(2);

        return new StringValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return format(e, context, "[[source:substr(~g, ~g, '~g')]]'~g'",
                        fromValue, toValue, strValue, this);
            }

            @Override
            protected String _get(E e, Context context) {
                int from = ((Long) fromValue.get(e, context)).intValue();
                int to = ((Long) toValue.get(e, context)).intValue();
                String str = (String) strValue.get(e, context);
                return str.substring(from, to);
            }
        };
    }

}
