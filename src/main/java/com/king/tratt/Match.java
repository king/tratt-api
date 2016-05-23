// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

class Match {
    private final Operator operator;
    private final int index;
    private final boolean usePreOperator;

    Match(Operator operator, int index) {
        this(operator, index, false);
    }

    Match(Operator operator, int index, boolean usePreOperator) {
        this.operator = operator;
        this.index = index;
        this.usePreOperator = usePreOperator;
    }

    Match getForPreOperator() {
        return new Match(operator, index, true);
    }

    Operator getOperator() {
        return usePreOperator ? operator.getPreOperator() : operator;
    }

    int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Match{" +
                "operator=" + operator +
                ", index=" + index +
                '}';
    }
}
