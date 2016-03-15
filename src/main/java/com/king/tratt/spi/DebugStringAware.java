package com.king.tratt.metadata.spi;

public interface DebugStringAware<E extends Event> {

    String toDebugString(E e, Context context);

}
