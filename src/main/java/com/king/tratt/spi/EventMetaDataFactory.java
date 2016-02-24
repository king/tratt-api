/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt.spi;

public interface EventMetaDataFactory<T extends EventMetaData> {

    T getEventMetaData(String eventName);

    /**
     * Do not override this.
     *
     * @return null
     */
    default T unrecognizedEventMetaData() {
        return null;
    }
}
