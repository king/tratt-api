package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.DebugStringAware;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.SufficientContextAware;
import com.king.tratt.spi.Value;

abstract class Matcher<E extends Event> implements DebugStringAware<E>, SufficientContextAware<E> {
    private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);
    private final List<? extends SufficientContextAware<E>> awares;
    private final String name;

    private Matcher(SufficientContextAware<E> aware) {
        this("", Arrays.asList(aware));
    }

    private Matcher(String name, SufficientContextAware<E> aware) {
        this(name, Arrays.asList(aware));
    }

    private Matcher(SufficientContextAware<E> aware1, SufficientContextAware<E> aware2) {
        this("", asList(aware1, aware2));
    }

    private Matcher(String name, SufficientContextAware<E> aware1, SufficientContextAware<E> aware2) {
        this(name, asList(aware1, aware2));
    }

    private Matcher(List<? extends SufficientContextAware<E>> awares) {
        this("", awares);
    }

    private Matcher(String name, List<? extends SufficientContextAware<E>> awares) {
        if(name == null){
            throw util.nullArgumentError("name");
        }
        this.name = name;
        this.awares = awares;
    }

    abstract boolean _matches(E e, Context context);

    abstract String _toDebugString(E e, Context context);

    final boolean matches(E e, Context context) {
        try {
            return _matches(e, context);
        } catch (Throwable t) {
            String message = "Crashed when performing this match:  %s\n"
                    + "event: %s, context: %s; exception:";
            LOG.error(String.format(message, _toDebugString(e, context), e, context), t);
            return false;
        }
    }

    @Override
    public String toDebugString(E e, Context context) {
        if (matches(e, context)) {
            return _toDebugString(e, context);
        } else {
            return " >> " + _toDebugString(e, context) + " << ";
        }
    }

    @Override
    public boolean hasSufficientContext(Context context) {
        return util.hasSufficientContext(context, awares);
    }
    
    @Override
    public String toString() {
        return name + awares;
    }
    
    /*
     * Anonymous Matchers goes below
     */

    static <E extends Event> Matcher<E> lessThan(final Value<E> left, final Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                return (long) left.get(e, context) < (long) right.get(e, context);
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(~d < ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "<" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> intBoolean(Value<E> value) {
        return new Matcher<E>("intBoolean:", value) {

            @Override
            protected boolean _matches(E e, Context context) {
                return !"0".equals(value.asString(e, context));
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(0 != ~d)", value);
            }
        };
    }

    static <E extends Event> Matcher<E> and(Matcher<E> left, Matcher<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                return left.matches(e, context) && right.matches(e, context);
            }

            @Override
            /*
             * bypass the super class, as we don't want error signs
             * surrounding the debug string form this matcher.
             */
            public String toDebugString(E e, Context context) {
                return _toDebugString(e, context);
            }

            @Override
            protected String _toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d && ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "&&" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> or(Matcher<E> left, Matcher<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                return left.matches(e, context) || right.matches(e, context);
            }

            @Override
            /*
             * bypass the super class, as we don't want error signs
             * surrounding the debug string form this matcher.
             */
            public String toDebugString(E e, Context context) {
                return _toDebugString(e, context);
            }

            @Override
            protected String _toDebugString(Event e, Context context) {
                return util.format(e, context, "(~d || ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "||" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> equal(Value<E> left, Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                String l = left.asString(e, context);
                String r = right.asString(e, context);
                return l.equals(r);
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(~d == ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "==" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> notEqual(Value<E> left, Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                String l = left.asString(e, context);
                String r = right.asString(e, context);
                return !l.equals(r);
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(~d != ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "!=" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> lessThanOrEqual(Value<E> left, Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                return (long) left.get(e, context) <= (long) right.get(e, context);
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(~d <= ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "<=" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> greaterThan(Value<E> left, Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                return (long) left.get(e, context) > (long) right.get(e, context);
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(~d > ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + ">" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> greaterThanOrEqual(Value<E> left, Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean _matches(E e, Context context) {
                return (long) left.get(e, context) >= (long) right.get(e, context);
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return util.format(e, context, "(~d >= ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + ">=" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> in(Value<E> value, List<Value<E>> values) {
        return new Matcher<E>("IN", util.concat(value, values)) {

            @Override
            protected boolean _matches(E e, Context context) {
                final String v = value.asString(e, context);
                for (Value<E> l : values) {
                    if (v.equals(l.asString(e, context))) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                String joinedValues = util.formatJoin(e, context, ", ", "~d", values);
                return util.format(e, context, "(~d IN [~s])", value, joinedValues);
            }
        };
    }

    static <E extends Event> Matcher<E> not(Matcher<E> matcher) {
        return new Matcher<E>("!", matcher) {

            @Override
            protected boolean _matches(E e, Context context) {
                return !matcher.matches(e, context);
            }
            
            @Override
            protected String _toDebugString(E e, Context context) {
                /*
                 * Do not call "matcher.toDebugString(...) as it will
                 * be surrounded by false error signs ( >> << ).
                 * In this case underlying matcher should fail
                 * for this matcher to pass.
                 */
                return "!" + matcher._toDebugString(e, context);
            }
        };
    }

    static <E extends Event> Matcher<E> functionMatcher(Value<E> function) {
        return new Matcher<E>(function) {

            @Override
            protected boolean _matches(E e, Context context) {
                return Boolean.parseBoolean(function.asString(e, context));
            }

            @Override
            protected String _toDebugString(E e, Context context) {
                return function.toDebugString(e, context);
            }
            
            @Override
            public String toString() {
                return function.toString();
            }
        };
    }
}
