package com.king.tratt;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

import java.util.function.Function;

import static com.king.tratt.Tratt.util;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;

/*
 * Static factory method for various Values:
 */
class Values {

    private static final String SOURCE_CONSTANT = "[[source:constant]]%s";

    Value constant(Object value) {
        Value result = parseValue(value,
                this::constantLong,
                this::constantString,
                this::constantBoolean);
        return result;
    }

    Value constantString(final String str) {
        return new Value() {

            @Override
            public String toDebugString(Event e, Context context) {
                return String.format(SOURCE_CONSTANT, "'" + str + "'");
            }

            @Override
            protected String getImp(Event e, Context context) {
                return str;
            }

            @Override
            public String toString() {
                return toDebugString(null, null);
            }
        };
    }

    Value constantLong(long l) {
        return new Value() {

            @Override
            public String toDebugString(Event e, Context context) {
                return String.format(SOURCE_CONSTANT, l);
            }

            @Override
            protected Long getImp(Event e, Context context) {
                return l;
            }

            @Override
            public String toString() {
                return toDebugString(null, null);
            }
        };
    }

    Value constantBoolean(boolean b) {
        return new Value() {

            @Override
            public String toDebugString(Event e, Context context) {
                return String.format(SOURCE_CONSTANT, b);
            }

            @Override
            protected Boolean getImp(Event e, Context context) {
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

    Value modulus(final Value value, final Value modulus) {
        return new Value(value, modulus) {
            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d % ~d)~g", value, modulus, this);
            }

            @Override
            protected Long getImp(Event e, Context context) {
                return (long) value.get(e, context) % (long) modulus.get(e, context);
            }

            @Override
            public String toString() {
                return value + "%" + modulus;
            }
        };
    }

    Value sum(Value left, Value right) {
        return new Value(left, right) {

            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d + ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(Event e, Context context) {
                return (long) left.get(e, context) + (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "+" + right;
            }
        };
    }

    Value subtract(Value left, Value right) {
        return new Value(left, right) {

            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d - ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(Event e, Context context) {
                return (long) left.get(e, context) - (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "-" + right;
            }
        };
    }

    Value multiply(Value left, Value right) {
        return new Value(left, right) {

            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d * ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(Event e, Context context) {
                return (long) left.get(e, context) * (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "*" + right;
            }
        };
    }

    Value divide(Value left, Value right) {
        return new Value(left, right) {

            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d / ~d)~g", left, right, this);
            }

            @Override
            protected Long getImp(Event e, Context context) {
                return (long) left.get(e, context) / (long) right.get(e, context);
            }

            @Override
            public String toString() {
                return left + "/" + right;
            }
        };
    }

    Value context(String name) {
        return new Value() {

            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "[[source:context.~s]]~g", name, this);
            }

            @Override
            protected Object getImp(Event e, Context context) {
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

    Value eventId() {
        return new Value() {

            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "[[source:event.id]]~g", this);
            }

            @Override
            protected String getImp(Event e, Context context) {
                return e.getId();
            }

            @Override
            public String toString() {
                return "[[source:event.id]]";
            }
        };
    }
}
