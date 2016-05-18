/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

public interface ValueFactory {

    Value getValue(String eventName, String nodeName);

    default Value unknown() {
        return null;
    }

}
