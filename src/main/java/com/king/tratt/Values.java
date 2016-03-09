package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;

/*
 * Static factory method for various Values:
 */
public class Values {

    private static final String SOURCE_CONSTANT = "[[source:constant]]%s";

    static <E extends Event> Value<E> constant(String value) {
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

    static <E extends Event> Value<E> constantString(final String str) {
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

    static <E extends Event> Value<E> constantLong(long l) {
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

    static <E extends Event> Value<E> constantBoolean(boolean b) {
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

    public static <E extends Event> Value<E> plain(Object value) {
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
    static <E extends Event> Value<E> plainString(final String str) {
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

    static <E extends Event> Value<E> plainLong(long l) {
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

    static <E extends Event> Value<E> plainBoolean(boolean b) {
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

    static <E extends Event> Value<E> modulus(final Value<E> value, final Value<E> modulus) {
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

    static <E extends Event> Value<E> sum(Value<E> left, Value<E> right) {
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

    static <E extends Event> Value<E> subtract(Value<E> left, Value<E> right) {
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

    static <E extends Event> Value<E> multiply(Value<E> left, Value<E> right) {
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

    static <E extends Event> Value<E> divide(Value<E> left, Value<E> right) {
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

    static <E extends Event> Value<E> contextValue(String name) {
        return new Value<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "[[source:context.~s]]~g", name, this);
            }

            @Override
            protected Object _get(E e, Context context) {
                // TODO context.get(...) should return Value<E>!
                return context.get(name);
            }

            @Override
            public boolean hasSufficientContext(Context context) {
                return context.containsKey(name);
            }
        };
    }

    static <E extends Event> Value<E> eventId() {
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
