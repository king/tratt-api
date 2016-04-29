/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

/**
 * Base exceptions for all exceptions thrown from this api package.
 */
public class TrattException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    TrattException(String message) {
        super(message);
    }

    TrattException(String message, Throwable t) {
        super(message, t);
    }

}
