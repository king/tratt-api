package com.king.tratt;

class Operator {
    public static enum Type {
        PRE,
        MIDDLE,
        GROUPING_START,
        GROUPING_END,
        ARRAY_START,
        ARRAY_END,
        ARRAY_DELIMITER,
        STRING_SIGN,
        FUNCTION_START,
    }

    private final String symbol;
    private final Type type;
    private final int strength;
    private final boolean spaceSensitive;
    private final boolean caseSensitive;
    private final Operator closingOperator;
    private final Operator preOperator;

    Operator(String symbol, int middleStrength, int preStrength) {
        this(symbol, Type.MIDDLE, middleStrength, false, false, null, preStrength);
    }

    Operator(String symbol, Type type, int strength) {
        this(symbol, type, strength, false, false, null, -1);
    }

    Operator(String symbol, Type type, int strength, boolean spaceSensitive, boolean caseSensitive,
            Operator closingOperator) {
        this(symbol, type, strength, spaceSensitive, caseSensitive, closingOperator, -1);
    }

    Operator(String symbol, Type type, int strength, boolean spaceSensitive, boolean caseSensitive,
            Operator closingOperator, int preAlternativeStrength) {
        this.symbol = symbol;
        this.type = type;
        this.strength = strength;
        this.spaceSensitive = spaceSensitive;
        this.caseSensitive = caseSensitive;
        this.closingOperator = closingOperator;
        if(preAlternativeStrength >= 0) {
            preOperator = new Operator(symbol, Type.PRE, preAlternativeStrength);
        } else {
            preOperator = null;
        }
    }


    String getSymbol() {
        return symbol;
    }

    Type getType() {
        return type;
    }

    int getStrength() {
        return strength;
    }

    boolean isSpaceSensitive() {
        return spaceSensitive;
    }

    boolean isCaseSensitive() {
        return caseSensitive;
    }

    Operator getClosingOperator() {
        return closingOperator;
    }

    Operator getPreOperator() {
        return preOperator;
    }

    @Override
    public String toString() {
        return type+"_"+symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Operator)) {
            return false;
        }

        Operator operator = (Operator) o;

        if (caseSensitive != operator.caseSensitive) {
            return false;
        }
        if (spaceSensitive != operator.spaceSensitive) {
            return false;
        }
        if (strength != operator.strength) {
            return false;
        }
        if (!closingOperator.equals(operator.closingOperator)) {
            return false;
        }
        if (!preOperator.equals(operator.preOperator)) {
            return false;
        }
        if (!symbol.equals(operator.symbol)) {
            return false;
        }
        if (type != operator.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = symbol.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + strength;
        result = 31 * result + (spaceSensitive ? 1 : 0);
        result = 31 * result + (caseSensitive ? 1 : 0);
        result = 31 * result + closingOperator.hashCode();
        result = 31 * result + preOperator.hashCode();
        return result;
    }
}
