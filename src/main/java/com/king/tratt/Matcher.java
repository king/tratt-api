/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
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

abstract class Matcher implements DebugStringAware, SufficientContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);
    private final List<? extends SufficientContextAware> awares;

    private Matcher(SufficientContextAware aware) {
        this(Arrays.asList(aware));
    }

    private Matcher(SufficientContextAware aware1, SufficientContextAware aware2) {
        this(asList(aware1, aware2));
    }

    private Matcher(List<? extends SufficientContextAware> awares) {
        this.awares = awares;
    }

    abstract boolean matchesImp(Event e, Context context);

    abstract String toDebugStringImp(Event e, Context context);

    final boolean matches(Event event, Context context) {
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
    public String toDebugString(Event event, Context context) {
        if (matches(event, context)) {
            return toDebugStringImp(event, context);
        } else {
            try {
                return format(" >> %s << ", toDebugStringImp(event, context));
            } catch (Exception ex) {
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
        return awares.stream().allMatch(e -> e.hasSufficientContext(context));
    }

    /*
     * Anonymous Matchers goes below
     */
    static Matcher lessThan(final Value left, final Value right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return (long) left.get(e, context) < (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(~d < ~d)", left, right);
            }

            @Override
            public String toString() {
                return left + "<" + right;
            }
        };
    }

    static Matcher intBoolean(Value value) {
        return new Matcher(value) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return !"0".equals(value.asString(e, context));
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(0 != ~d)", value);
            }

            @Override
            public String toString() {
                return "0!=" + value;
            }
        };
    }

    static Matcher and(Matcher left, Matcher right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return left.matches(e, context) && right.matches(e, context);
            }

            @Override
            // bypass the super class, as we don't want error signs
            // surrounding the debug string form this matcher.
            public String toDebugString(Event e, Context context) {
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

    static Matcher or(Matcher left, Matcher right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return left.matches(e, context) || right.matches(e, context);
            }

            @Override
            // bypass the super class, as we don't want error signs
            // surrounding the debug string form this matcher.
            public String toDebugString(Event e, Context context) {
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

    static Matcher equalTo(Value left, Value right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                String l = left.asString(e, context);
                String r = right.asString(e, context);
                return l.equals(r);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(~d == ~d)", left, right);
            }

            @Override
            public String toString() {
                return left + "==" + right;
            }
        };
    }

    static Matcher notEqual(Value left, Value right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                String l = left.asString(e, context);
                String r = right.asString(e, context);
                return !l.equals(r);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(~d != ~d)", left, right);
            }

            @Override
            public String toString() {
                return left + "!=" + right;
            }
        };
    }

    static Matcher lessThanOrEqual(Value left, Value right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return (long) left.get(e, context) <= (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(~d <= ~d)", left, right);
            }

            @Override
            public String toString() {
                return left + "<=" + right;
            }
        };
    }

    static Matcher greaterThan(Value left, Value right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return (long) left.get(e, context) > (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(~d > ~d)", left, right);
            }

            @Override
            public String toString() {
                return left + ">" + right;
            }
        };
    }

    static Matcher greaterThanOrEqual(Value left, Value right) {
        return new Matcher(left, right) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return (long) left.get(e, context) >= (long) right.get(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return util.format(e, context, "(~d >= ~d)", left, right);
            }

            @Override
            public String toString() {
                return left + ">=" + right;
            }
        };
    }

    static Matcher not(Matcher matcher) {
        return new Matcher(matcher) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return !matcher.matches(e, context);
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                // Do not call "matcher.toDebugString(...) as it will
                // be surrounded by false error signs ( >> << ).
                // In this case underlying matcher should fail
                // for this matcher to pass.
                return "!" + matcher.toDebugStringImp(e, context);
            }

            @Override
            public String toString() {
                return "!" + matcher;
            }
        };
    }

    static Matcher functionMatcher(Value function) {
        return new Matcher(function) {

            @Override
            protected boolean matchesImp(Event e, Context context) {
                return Boolean.parseBoolean(function.asString(e, context));
            }

            @Override
            protected String toDebugStringImp(Event e, Context context) {
                return function.toDebugString(e, context);
            }

            @Override
            public String toString() {
                return function.toString();
            }
        };
    }
}
