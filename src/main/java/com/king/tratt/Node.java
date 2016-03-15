/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt;

import java.util.Arrays;
import java.util.List;

/**
 * Created by magnus.ramstedt on 26/11/14.
 */
class Node {
    private final List<Node> subNodes;
    private final Operator operator;
    private final Range range;
    private final String literalString;

    Node(Range range) {
        this(null, null, range, null);
    }

    Node(String literalString) {
        this(null, null, null, literalString);
    }

    Node(Operator operator, Range range, Node... subNodes) {
        this(Arrays.asList(subNodes), operator, range, null);
    }

    Node(List<Node> subNodes, Operator operator, Range range) {
        this(subNodes, operator, range, null);
    }

    private Node(List<Node> subNodes, Operator operator, Range range, String literalString) {
        this.subNodes = subNodes;
        this.operator = operator;
        this.range = range;
        this.literalString = literalString;
    }

    String getExpression() {
        return literalString == null ? range.getExpression(operator == null || !operator.isCaseSensitive()) : literalString;
    }

    boolean isLiteralString() {
        return literalString != null;
    }

    boolean isNumberFormatted() {
        if (literalString != null) {
            return false;
        }
        try {
            Double.parseDouble(getExpression());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    Operator.Type getOperatorType() {
        return operator==null ? null : operator.getType();
    }

    String getOperatorSymbol() {
        return operator == null ? null : operator.getSymbol();
    }

    Node getNode(int index) {
        return subNodes.get(index);
    }

    List<Node> getSubNodes() {
        return subNodes;
    }

    String getCurrentString() {
        return range.getPreviousExpression()+"@@"+range.getExpression();
    }

    @Override
    public String toString() {
        if(operator == null) {
            return "{"+getExpression()+"}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\"").append(operator.getSymbol());
        if(operator.getClosingOperator() != null) {
            sb.append(operator.getClosingOperator().getSymbol());
        }
        sb.append("\",");
        for(int i = 0 ; i < subNodes.size() ; i++) {
            if(i>0) {
                sb.append(", ");
            }
            sb.append(subNodes.get(i));
        }

        sb.append("}");
        return sb.toString();
    }
}
