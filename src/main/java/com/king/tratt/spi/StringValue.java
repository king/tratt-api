package com.king.tratt.spi;

import java.util.List;

public abstract class StringValue<E extends Event> extends Value<E> {

    @SafeVarargs
    public StringValue(SufficientContextAware<E>... values) {
        super(values);
    }

    public StringValue(List<? extends SufficientContextAware<E>> awares) {
        super(awares);
    }

    /*
     * Make method final, as we don't want clients to override it.
     */
    @Override
    final public Object get(E e, Context context) {
        return super.get(e, context);
    }

    @Override
    protected abstract String _get(E e, Context context);
}
