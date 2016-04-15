/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

class OperatorMatcher {
    private final Collection<Operator> operators;
    private char literalStringEscapeChar;

    OperatorMatcher(Collection<Operator> operators, char literalStringEscapeChar) {
        this.operators = operators;
        this.literalStringEscapeChar = literalStringEscapeChar;
    }


    List<Match> matches(String expression) {
    	List<Match> operators = getOperatorMatches(expression);
        modifyPreOperators(expression, operators);
        return operators;
    }

    private List<Match> getOperatorMatches(String expression) {
        SortedSet<Match> matchSet = new TreeSet<Match>(new Comparator<Match>() {
            @Override
            public int compare(Match o1, Match o2) {
                return o1.getIndex()-o2.getIndex();
            }
        });
        for(Operator o : operators) {
            for(int index = expression.indexOf(o.getSymbol(),0) ; index >= 0 ; index = expression.indexOf(o.getSymbol(),index+1)) {
                if (!(o.getType() == Operator.Type.STRING_SIGN && index > 0 && expression.charAt(index-1) == literalStringEscapeChar)) {
                    matchSet.add(new Match(o, index));
                }
            }
        }

        return new ArrayList<>(matchSet);
    }

    private void modifyPreOperators(String expression, List<Match> operators) {
        Match last = null;
        for(int i = 0 ; i < operators.size() ; i++) {
            final Match m = operators.get(i);
            if (alterToPreOperator(expression, last, m)) {
                final Match altered = m.getForPreOperator();
                operators.set(i, altered);
                last = altered;
            } else {
                last = m;
            }
        }
    }

    private boolean alterToPreOperator(String expression, Match last, Match m) {
        if(m.getOperator().getPreOperator() == null) {
            return false;
        }
        if(last != null) {
            switch (last.getOperator().getType()) {
            case GROUPING_END:
            case ARRAY_END:
            case STRING_SIGN:
                return false;
            default:
                // ignore
            }
        }

        final int lastIndex = last == null ? indexOfNonSpace(expression, 0) : indexOfNonSpace(expression, last.getIndex()+last.getOperator().getSymbol().length());

        return lastIndex == m.getIndex();
    }

    private int indexOfNonSpace(String expression, int i) {
        for(; expression.charAt(i) == '\t' || expression.charAt(i) == ' ' ; i++) {
            ;
        }
        return i;
    }
}
