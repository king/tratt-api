/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
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