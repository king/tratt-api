package com.king.tratt.spi;

public interface SufficientContextAware<E extends Event> {

    boolean hasSufficientContext(Context context);
}
