package com.king.tratt;

class MatchExpressionParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatchExpressionParseException(String message) {
        super(message);
    }

    public MatchExpressionParseException(String message, Throwable throwable) {
        super(message, throwable);
    }

}