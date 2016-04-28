/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

public interface ValueFactory {

    Value getValue(String eventName, String parameterName);

    default Value notFound() {
        return null;
    }

}
