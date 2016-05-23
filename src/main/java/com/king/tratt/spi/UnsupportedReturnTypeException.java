// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt.spi;

public class UnsupportedReturnTypeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    UnsupportedReturnTypeException(String message) {
        super(message);
    }
}
