package com.king.tratt.metadata.spi;

public interface Context {

    boolean containsKey(String name);

    Object get(String name);
}
