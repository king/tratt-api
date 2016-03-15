package com.king.tratt.metadata.spi;

public interface SufficientContextAware<E extends Event> {

    boolean hasSufficientContext(Context context);
}
