package com.king.tratt.spi;

import java.util.List;

public abstract class DynamicValue<E extends Event> extends Value<E> {

    @SafeVarargs
    public DynamicValue(SufficientContextAware<E>... values) {
        super("", values);
    }

    @SafeVarargs
    public DynamicValue(String name, SufficientContextAware<E>... values) {
        super(name, values);
    }

    public DynamicValue(List<? extends SufficientContextAware<E>> awares) {
        super(awares);
    }

    public DynamicValue(String name, List<? extends SufficientContextAware<E>> awares) {
        super(name, awares);
    }

    /*
     * Make method final, as we don't want clients to override it.
     */
    @SuppressWarnings("unchecked")
    @Override
    final public Object get(E e, Context context) {
        // TODO move this to Value?
        Object o = super.get(e, context);
        while (o instanceof Value) {
            o = ((Value<E>) o).get(e, context);
        }

        return o;
    }

    //    final public Value<E> getValue(E e, context ) TODO is this needed?

    @Override
    protected abstract Value<E> _get(E e, Context context);
}
