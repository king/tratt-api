package com.king.tratt.spi;

import java.util.List;

public abstract class BooleanValue<E extends Event> extends Value<E> {

    @SafeVarargs
    public BooleanValue(SufficientContextAware<E>... values) {
        super("", values);
    }

    @SafeVarargs
    public BooleanValue(String name, SufficientContextAware<E>... values) {
        super(name, values);
    }

    public BooleanValue(List<? extends SufficientContextAware<E>> awares) {
        super("", awares);
    }

    public BooleanValue(String name, List<? extends SufficientContextAware<E>> awares) {
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
    protected abstract Boolean _get(E e, Context context);

}