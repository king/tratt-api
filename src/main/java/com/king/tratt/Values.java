package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;

import java.util.function.Function;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

/*
 * Static factory method for various Values:
 */
class Values {

    private static final String SOURCE_CONSTANT = "[[source:constant]]%s";

    <E extends Event> Value<E> constant(String value) {
        Value<E> result = parseValue(value,
                this::constantLong,
                this::constantString,
                this::constantBoolean);
        return result;
    }

    <E extends Event> Value<E> constantString(final String str) {
        return new Value<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format(SOURCE_CONSTANT, "'" + str + "'");
            }

            @Override
            protected String getImp(E e, Context context) {
                return str;
            }

            @Override
            public String toString() {
                return toDebugString(null, null);
            }
        };
    }

    <E extends Event> Value<E> constantLong(long l) {
        return new Value<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format(SOURCE_CONSTANT, l);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return l;
            }

            @Override
            public String toString() {
                return toDebugString(null, null);
            }
        };
    }

    <E extends Event> Value<E> constantBoolean(boolean b) {
        return new Value<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return String.format(SOURCE_CONSTANT, b);
            }

            @Override
            protected Boolean getImp(E e, Context context) {
                return b;
            }

            @Override
            public String toString() {
                return toDebugString(null, null);
            }
        };
    }

    Object parseSupportedType(String value) {
        return parseValue(value, l -> l, s -> s, b -> b);
    }

    String quoted(Object value) {
        return parseValue(value, String::valueOf,
                s -> String.format("'%s'", s), String::valueOf);
    }


    private <T> T parseValue(Object value, Function<Long, T> longFunc,
            Function<String, T> strFunc, Function<Boolean, T> boolFunc) {
        if (value instanceof Long) {
            return longFunc.apply((long) value);
        } else if (value instanceof Boolean) {
            return boolFunc.apply((boolean) value);
        } else if (value instanceof String) {
            String str = (String) value;
            if (util.isLong(str)) {
                return longFunc.apply(parseLong(str));
            } else if (util.isBoolean(str)) {
                return boolFunc.apply(parseBoolean(str));
            }
            return strFunc.apply(str);
        }
        String message = "Unsupported type for Value.get(...): '%s' [%s]";
        throw new IllegalStateException(String.format(message,
                value.getClass(), value));
    }

    <E extends Event> Value<E> modulus(final Value<E> value, final Value<E> modulus) {
        return new Value<E>(value, modulus) {
            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d % ~d)~g", value, modulus, this);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return (long) value.get(e, context) % (long) modulus.get(e, context);
            }

            @Override
            public String toString() {
                return value + "%" + modulus;
            }
        };
    }

    <E extends Event> Value<E> sum(Value<E> left, Value<E> right) {
        return new Value<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d + ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return (long) left.get(e, context) + (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "+" + right;
            }
        };
    }

    <E extends Event> Value<E> subtract(Value<E> left, Value<E> right) {
        return new Value<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d - ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return (long) left.get(e, context) - (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "-" + right;
            }
        };
    }

    <E extends Event> Value<E> multiply(Value<E> left, Value<E> right) {
        return new Value<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d * ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return (long) left.get(e, context) * (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "*" + right;
            }
        };
    }

    <E extends Event> Value<E> divide(Value<E> left, Value<E> right) {
        return new Value<E>(left, right) {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "(~d / ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return (long) left.get(e, context) / (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "/" + right;
            }
        };
    }

    <E extends Event> Value<E> context(String name) {
        return new Value<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "[[source:context.~s]]~g", name, this);
            }

            @Override
            protected Object getImp(E e, Context context) {
                if (hasSufficientContext(context)) {
                    return context.get(name);
                }
                throw new IllegalStateException("[Insufficient Context!]");
            }

            @Override
            public boolean hasSufficientContext(Context context) {
                return context.containsKey(name);
            }

            @Override
            public String toString() {
                return String.format("[[source:context.%s]]", name);
            }

        };
    }

    <E extends Event> Value<E> eventId() {
        return new Value<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "[[source:event.id]]~g", this);
            }

            @Override
            protected Long getImp(E e, Context context) {
                return e.getId();
            }

            @Override
            public String toString() {
                return "[[source:event.id]]";
            }
        };
    }
}
