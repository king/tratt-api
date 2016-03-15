package com.king.tratt.spi;

public interface Context {

    boolean containsKey(String name);

    Object get(String name);
}
