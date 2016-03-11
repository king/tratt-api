package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static com.king.tratt.Tratt.values;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;

import com.king.tratt.metadata.spi.BooleanValue;
import com.king.tratt.metadata.spi.Context;
import com.king.tratt.metadata.spi.DynamicValue;
import com.king.tratt.metadata.spi.Event;
import com.king.tratt.metadata.spi.LongValue;
import com.king.tratt.metadata.spi.StringValue;
import com.king.tratt.metadata.spi.Value;

/*
 * Static factory method for various Values:
 */
public class Values {

    private static final String SOURCE_CONSTANT = "[[source:constant]]%s";

    <E extends Event> Value<E> constant(String value) {
        if (util.isLong(value)) {
            return constantLong(parseLong(value));
        } else if (util.isBoolean(value)) {
            return constantBoolean(parseBoolean(value));
        }
        return constantString(value);
    }

    /*
     * Constant values
     */

    <E extends Event> Value<E> constantString(final String str) {
        return new StringValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format(SOURCE_CONSTANT, "'" + str + "'");
            }

            @Override
            protected String _get(E e, Context context) {
                return str;
            }
        };
    }

    <E extends Event> Value<E> constantLong(long l) {
        return new LongValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format(SOURCE_CONSTANT, l);
            }

            @Override
            protected Long _get(E e, Context context) {
                return l;
            }
        };
    }

    <E extends Event> Value<E> constantBoolean(boolean b) {
        return new BooleanValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format(SOURCE_CONSTANT, b);
            }

            @Override
            protected Boolean _get(E e, Context context) {
                return b;
            }
        };
    }

    public <E extends Event> Value<E> plain(Object value) {
        if (value instanceof Long) {
            return plainLong((long) value);
        } else if (value instanceof Boolean) {
            return plainBoolean((boolean) value);
        } else if (value instanceof String) {
            String str = (String) value;
            if (util.isLong(str)) {
                return plainLong(parseLong(str));
            } else if (util.isBoolean(str)) {
                return plainBoolean(parseBoolean(str));
            }
            return plainString(str);
        }
        String message = "Unsupported return type for Value.get(...): '%s' [%s]";
        throw new IllegalStateException(String.format(message, value, value.getClass()));
    }

    /*
     * Plain values
     */
    <E extends Event> Value<E> plainString(final String str) {
        return new StringValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format("'%s'", str);
            }

            @Override
            protected String _get(E e, Context context) {
                return str;
            }
        };
    }

    <E extends Event> Value<E> plainLong(long l) {
        return new LongValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.valueOf(l);
            }

            @Override
            protected Long _get(E e, Context context) {
                return l;
            }
        };
    }

    <E extends Event> Value<E> plainBoolean(boolean b) {
        return new BooleanValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.valueOf(b);
            }

            @Override
            protected Boolean _get(E e, Context context) {
                return b;
            }
        };
    }

    <E extends Event> Value<E> modulus(final Value<E> value, final Value<E> modulus) {
        return new LongValue<E>(value, modulus) {
            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d % ~d)~g", value, modulus, this);
            }

            @Override
            protected Long _get(E e, Context context) {
                return (long) value.get(e, context) % (long) modulus.get(e, context);
            }
        };
    }

    <E extends Event> Value<E> sum(Value<E> left, Value<E> right) {
        return new LongValue<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d + ~d)~g", left, right, this);
            }

            @Override
            protected Long _get(E e, Context context) {
                return (long) left.get(e, context) + (long) right.get(e, context);
            }
        };
    }

    <E extends Event> Value<E> subtract(Value<E> left, Value<E> right) {
        return new LongValue<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d - ~d)~g", left, right, this);
            }

            @Override
            protected Long _get(E e, Context context) {
                return (long) left.get(e, context) - (long) right.get(e, context);
            }

        };
    }

    <E extends Event> Value<E> multiply(Value<E> left, Value<E> right) {
        return new LongValue<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d * ~d)~g", left, right, this);
            }

            @Override
            protected Long _get(E e, Context context) {
                return (long) left.get(e, context) * (long) right.get(e, context);
            }
        };
    }

    <E extends Event> Value<E> divide(Value<E> left, Value<E> right) {
        return new LongValue<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d / ~d)~g", left, right, this);
            }

            @Override
            protected Long _get(E e, Context context) {
                return (long) left.get(e, context) / (long) right.get(e, context);
            }
        };
    }

    <E extends Event> Value<E> context(String name) {
        return new DynamicValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "[[source:context.~s]]~g", name, this);
            }

            @Override
            protected Value<E> _get(E e, Context context) {
                return values.plain(context.get(name));
            }

            @Override
            public boolean hasSufficientContext(Context context) {
                return context.containsKey(name);
            }
        };
    }

    <E extends Event> Value<E> eventId() {
        return new LongValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "[[source:event.id]]~g", this);
            }

            @Override
            protected Long _get(E e, Context context) {
                return e.getId();
            }
        };
    }

}
