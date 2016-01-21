package com.king.tratt;

class Match {
    private final Operator operator;
    private final int index;
    private final boolean usePreOperator;

    public Match(Operator operator, int index) {
        this(operator, index, false);
    }

    public Match(Operator operator, int index, boolean usePreOperator) {
        this.operator = operator;
        this.index = index;
        this.usePreOperator = usePreOperator;
    }

    public Match getForPreOperator() {
        return new Match(operator, index, true);
    }

    public Operator getOperator() {
        return usePreOperator ? operator.getPreOperator() : operator;
    }

    public int getIndex() {
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
