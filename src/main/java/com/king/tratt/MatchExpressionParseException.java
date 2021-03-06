// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

class MatchExpressionParseException extends TrattException {

    private static final long serialVersionUID = 1L;

    MatchExpressionParseException(String message) {
        super(message);
    }

    MatchExpressionParseException(String message, Throwable throwable) {
        super(message, throwable);
    }

}