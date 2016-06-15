// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static com.king.tratt.Tratt.util;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

/*
 * Static factory methods for various Values:
 */
class Values {

    private static final String SOURCE_CONSTANT = "[[source:constant]]%s";

    Value constant(Object value) {
        return util.parseValue(value,
                this::constantLong,
                this::constantString,
                this::constantBoolean);
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

    Value modulus(final Value value, final Value modulus) {
        return new Value(value, modulus) {
            @Override
            public String toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d % ~d)~v", value, modulus, this);
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
                return util.format(e, context, "(~d + ~d)~v", left, right, this);
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
                return util.format(e, context, "(~d - ~d)~v", left, right, this);
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
                return util.format(e, context, "(~d * ~d)~v", left, right, this);
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
                return util.format(e, context, "(~d / ~d)~v", left, right, this);
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
                return util.format(e, context, "[[source:context.~s]]~v", name, this);
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
                return util.format(e, context, "[[source:event.id]]~v", this);
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
