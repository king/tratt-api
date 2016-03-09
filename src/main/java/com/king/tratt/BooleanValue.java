package com.king.tratt;

import java.util.List;

public abstract class BooleanValue<E extends Event> extends Value<E> {

    @SafeVarargs
    public BooleanValue(SufficientContextAware<E>... values) {
        super(values);
    }

    public BooleanValue(List<? extends SufficientContextAware<E>> awares) {
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
    protected abstract Boolean _get(E e, Context context);

}
