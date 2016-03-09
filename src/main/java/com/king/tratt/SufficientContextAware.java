package com.king.tratt;

public interface SufficientContextAware<E extends Event> {

    boolean hasSufficientContext(Context context);
}
