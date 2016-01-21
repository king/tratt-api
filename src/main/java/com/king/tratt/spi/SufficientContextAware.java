package com.king.tratt.spi;

public interface SufficientContextAware<E extends Event> {

    boolean hasSufficientContext(E e, Context context);
}
