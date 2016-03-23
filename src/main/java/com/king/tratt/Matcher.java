package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.lang.String.format;
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
    private final static Logger LOG = LoggerFactory.getLogger(Matcher.class);
    private final List<? extends SufficientContextAware<E>> awares;

    private Matcher(SufficientContextAware<E> aware) {
        this(Arrays.asList(aware));
    }

    private Matcher(SufficientContextAware<E> aware1, SufficientContextAware<E> aware2) {
        this(asList(aware1, aware2));
    }

    private Matcher(List<? extends SufficientContextAware<E>> awares) {
        this.awares = awares;
    }

    abstract boolean matchesImp(E e, Context context);

    abstract String toDebugStringImp(E e, Context context);

    final boolean matches(E event, Context context) {
        try {
            return matchesImp(event, context);
        } catch (Throwable t) {
            String message = "Unexpected crash! See underlying exceptions for more info.\n"
                    + "  matcher: %s; event: %s; context: %s";
            message = format(message, this, event, context);
            LOG.error(message, t);
            return false;
        }
    }
    
    @Override
    public String toDebugString(E event, Context context) {
        if (matches(event, context)) {
            return toDebugStringImp(event, context);
        } else {
            try {
                return format(" >> %s << ", toDebugStringImp(event, context));
            } catch (Exception ex){
                String message = "Unexpected crash! See underlying exceptions for more info.\n"
                        + "  matcher: %s; event: %s; context: %s";
                message = format(message, this, event, context);
                LOG.error(message, ex);
                String result = " >> @CRASH due to '%s' in '%s'. See console log for more info. << "; 
                return format(result, rootCause(ex).getMessage(), this); 
            }
        }
    }

    private Throwable rootCause(Throwable e) {
        Throwable cause = e.getCause();
        return cause == null ? e : rootCause(cause); 
    }

    @Override
    public boolean hasSufficientContext(Context context) {
        return util.hasSufficientContext(context, awares);
    }
    
    /*
     * Anonymous Matchers goes below
     */

    static <E extends Event> Matcher<E> lessThan(final Value<E> left, final Value<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean matchesImp(E e, Context context) {
                return (long) left.get(e, context) < (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
                return util.format(e, context, "(~d < ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + "<" + right;
            }
        };
    }

    static <E extends Event> Matcher<E> intBoolean(Value<E> value) {
        return new Matcher<E>(value) {

            @Override
            protected boolean matchesImp(E e, Context context) {
                return !"0".equals(value.asString(e, context));
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
                return util.format(e, context, "(0 != ~d)", value);
            }
            
            @Override
            public String toString() {
                return "0!=" + value;
            }
        };
    }

    static <E extends Event> Matcher<E> and(Matcher<E> left, Matcher<E> right) {
        return new Matcher<E>(left, right) {

            @Override
            protected boolean matchesImp(E e, Context context) {
                return left.matches(e, context) && right.matches(e, context);
            }

            @Override
            /*
             * bypass the super class, as we don't want error signs
             * surrounding the debug string form this matcher.
             */
            public String toDebugString(E e, Context context) {
                return toDebugStringImp(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
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
            protected boolean matchesImp(E e, Context context) {
                return left.matches(e, context) || right.matches(e, context);
            }

            @Override
            /*
             * bypass the super class, as we don't want error signs
             * surrounding the debug string form this matcher.
             */
            public String toDebugString(E e, Context context) {
                return toDebugStringImp(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
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
            protected boolean matchesImp(E e, Context context) {
                String l = left.asString(e, context);
                String r = right.asString(e, context);
                return l.equals(r);
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
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
            protected boolean matchesImp(E e, Context context) {
                String l = left.asString(e, context);
                String r = right.asString(e, context);
                return !l.equals(r);
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
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
            protected boolean matchesImp(E e, Context context) {
                return (long) left.get(e, context) <= (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
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
            protected boolean matchesImp(E e, Context context) {
                return (long) left.get(e, context) > (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
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
            protected boolean matchesImp(E e, Context context) {
                return (long) left.get(e, context) >= (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
                return util.format(e, context, "(~d >= ~d)", left, right);
            }
            
            @Override
            public String toString() {
                return left + ">=" + right;
            }
        };
    }

    // TODO remove?
    static <E extends Event> Matcher<E> in(Value<E> value, List<Value<E>> values) {
        return new Matcher<E>(util.concat(value, values)) {

            @Override
            protected boolean matchesImp(E e, Context context) {
                final String v = value.asString(e, context);
                for (Value<E> l : values) {
                    if (v.equals(l.asString(e, context))) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
                String joinedValues = util.formatJoin(e, context, ", ", "~d", values);
                return util.format(e, context, "(~d IN [~s])", value, joinedValues);
            }
            @Override
            public String toString() {
                return value + " IN " + values;
            }
        };
    }

    static <E extends Event> Matcher<E> not(Matcher<E> matcher) {
        return new Matcher<E>(matcher) {

            @Override
            protected boolean matchesImp(E e, Context context) {
                return !matcher.matches(e, context);
            }
            
            @Override
            protected String toDebugStringImp(E e, Context context) {
                /*
                 * Do not call "matcher.toDebugString(...) as it will
                 * be surrounded by false error signs ( >> << ).
                 * In this case underlying matcher should fail
                 * for this matcher to pass.
                 */
                return "!" + matcher.toDebugStringImp(e, context);
            }
            
            @Override
            public String toString() {
                return "!" + matcher;
            }
        };
    }

    static <E extends Event> Matcher<E> functionMatcher(Value<E> function) {
        return new Matcher<E>(function) {

            @Override
            protected boolean matchesImp(E e, Context context) {
                return Boolean.parseBoolean(function.asString(e, context));
            }

            @Override
            protected String toDebugStringImp(E e, Context context) {
                return function.toDebugString(e, context);
            }
            
            @Override
            public String toString() {
                return function.toString();
            }
        };
    }
}
