/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

class Range {
    private final int start;
    private final int end;
    private final String expression;

    Range(String expression) {
        this(expression, 0, expression.length());
    }

    private Range(String expression, int start, int end) {
        this.expression = expression;
        this.start = start;
        this.end = end;
    }

    int getStart() {
        return start;
    }

    int getEnd() {
        return end;
    }

    String getExpression() {
        return getExpression(false);
    }

    String getExpression(boolean trim) {
        final String out = expression.substring(start, end);
        if (trim) {
            return out.trim();
        }
        return out;
    }

    String getPreviousExpression() {
        return expression.substring(0, start);
    }

    Range getLeftSplit(Match match) {
        return new Range(expression, start, match.getIndex());
    }

    private int getEndIndex(Match match) {
        final int out = match.getIndex() + match.getOperator().getSymbol().length();
        return out;
    }

    Range getRightSplit(Match match) {
        return new Range(expression, getEndIndex(match), end);
    }

    Range getRegion(Match from, Match to) {
        return new Range(expression, getEndIndex(from), to.getIndex());
    }
}
