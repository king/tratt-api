package com.king.tratt.spi;

public interface ValueFactory<E extends Event> {

    Value<E> getValue(String eventName, String parameterName);

}
