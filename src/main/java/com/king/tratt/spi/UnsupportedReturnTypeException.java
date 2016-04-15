/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.spi;

public class UnsupportedReturnTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    UnsupportedReturnTypeException(String message) {
        super(message);
    }
}
