package com.king.tratt.spi;

public interface DebugStringAware<E extends Event> {

    String toDebugString(E e, Context context);

}
