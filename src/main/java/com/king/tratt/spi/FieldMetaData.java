/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt.spi;

// TODO remove ??
@Deprecated
public interface FieldMetaData {
    int getIndex();
    String getName();
    Class<?> getType();
}
