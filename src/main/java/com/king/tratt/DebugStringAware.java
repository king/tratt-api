package com.king.tratt;

public interface DebugStringAware<E extends Event> {

    String toDebugString(E e, Context context);

}
