package com.king.tratt.metadata.spi;

import java.util.List;

public abstract class LongValue<E extends Event> extends Value<E> {

    @SafeVarargs
    public LongValue(SufficientContextAware<E>... values) {
        super("", values);
    }

    @SafeVarargs
    public LongValue(String name, SufficientContextAware<E>... values) {
        super(name, values);
    }

    public LongValue(List<? extends SufficientContextAware<E>> awares) {
        super("", awares);
    }

    public LongValue(String name, List<? extends SufficientContextAware<E>> awares) {
        super(name, awares);
    }

    /*
     * Make method final, as we don't want clients to override it.
     */
    @Override
    final public Object get(E e, Context context) {
        return super.get(e, context);
    }

    @Override
    protected abstract Long _get(E e, Context context);

}
