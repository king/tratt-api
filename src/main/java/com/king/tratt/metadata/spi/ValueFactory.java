package com.king.tratt.metadata.spi;

public interface ValueFactory<E extends Event> {

    Value<E> getValue(String eventName, String parameterName);

    default Value<E> unrecognizedValue() {
        return null;
    }

}
