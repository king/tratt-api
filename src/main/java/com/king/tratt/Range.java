package com.king.tratt;

class Range {
    private final int start;
    private final int end;
    private final String expression;

    public Range(String expression) {
        this(expression, 0 , expression.length());
    }

    private Range(String expression, int start, int end) {
        this.expression = expression;
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getExpression() {
        return getExpression(false);
    }

    public String getExpression(boolean trim) {
        final String out = expression.substring(start, end);
        if(trim) {
            return out.trim();
        }
        return out;
    }

    public String getPreviousExpression() {
        return expression.substring(0, start);
    }

    public Range getLeftSplit(Match match) {
        return new Range(expression, start, match.getIndex());
    }

    private int getEndIndex(Match match) {
        final int out = match.getIndex()+match.getOperator().getSymbol().length();
        return out;
    }

    public Range getRightSplit(Match match) {
        return new Range(expression, getEndIndex(match), end);
    }

    public Range getRegion(Match from, Match to) {
        return new Range(expression, getEndIndex(from), to.getIndex());
    }
}
