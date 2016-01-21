/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt.spi;

public interface EventMetaDataProvider<T extends EventMetaData> {

    T getMetaData(String eventName);
}
