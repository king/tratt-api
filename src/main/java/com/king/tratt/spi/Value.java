package com.king.tratt.spi;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Value<E extends Event> implements DebugStringAware<E>, SufficientContextAware<E> {

    private static final Logger LOG = LoggerFactory.getLogger(Value.class);
    private List<? extends SufficientContextAware<E>> awares;

    /* For package private usage only. */
    @SafeVarargs
    Value(SufficientContextAware<E>... values) {
        this(Arrays.asList(values));
    }

    /* For package private usage only. */
    Value(List<? extends SufficientContextAware<E>> awares) {
        this.awares = awares;
    }

    protected abstract Object _get(E e, Context context);

    public Object get(E e, Context context) {
        try {
            return _get(e, context);
        } catch (Throwable t) {
            if (!hasSufficientContext(e, context)) {
                return "[Insufficient Context!]";
            }
            String message = "@ERROR on line: " + t.getStackTrace()[0] + ". " +
                    "message: %s\n event: %s; context: %s";
            message = String.format(message, t.getMessage(), e, context, this);
            LOG.error(message, t);
            return message;
        }
    }

    @Override
    public boolean hasSufficientContext(E e, Context context) {
        for (SufficientContextAware<E> aware : awares) {
            if (!aware.hasSufficientContext(e, context)) {
                return false;
            }
        }
        return true;
    };

    final public String asString(E e, Context context) {
        return String.valueOf(get(e, context));
    }
}
