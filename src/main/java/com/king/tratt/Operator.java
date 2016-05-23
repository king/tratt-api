// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

class Operator {
    public enum Type {
        PRE, MIDDLE, GROUPING_START, GROUPING_END, ARRAY_START, ARRAY_END, ARRAY_DELIMITER, STRING_SIGN, FUNCTION_START,
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
        if (preAlternativeStrength >= 0) {
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
        return type + "_" + symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Operator other = (Operator) obj;
        if (caseSensitive != other.caseSensitive)
            return false;
        if (closingOperator == null) {
            if (other.closingOperator != null)
                return false;
        } else if (!closingOperator.equals(other.closingOperator))
            return false;
        if (preOperator == null) {
            if (other.preOperator != null)
                return false;
        } else if (!preOperator.equals(other.preOperator))
            return false;
        if (spaceSensitive != other.spaceSensitive)
            return false;
        if (strength != other.strength)
            return false;
        if (symbol == null) {
            if (other.symbol != null)
                return false;
        } else if (!symbol.equals(other.symbol))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseSensitive ? 1231 : 1237);
        result = prime * result + ((closingOperator == null) ? 0 : closingOperator.hashCode());
        result = prime * result + ((preOperator == null) ? 0 : preOperator.hashCode());
        result = prime * result + (spaceSensitive ? 1231 : 1237);
        result = prime * result + strength;
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
}
