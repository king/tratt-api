// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

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
