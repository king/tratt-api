/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

class TdlNodeParser {
    private static final String ESCAPE_CHAR = "\\";
    private static final String LITERAL_QUOTE_CHAR = "'";
    private final Collection<Operator> operators;
    private final OperatorMatcher operatorMatcher;
    private final Pattern symbolForbiddenChars = Pattern.compile("[^a-zA-Z0-9_$]");
    private final Pattern functionName = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");

    TdlNodeParser() {
        operators = setupOperators();
        operatorMatcher = new OperatorMatcher(operators, ESCAPE_CHAR.charAt(0));
    }

    void addFunctions(Collection<String> names) {
        for (String name : names) {
            final Operator end = getFunctionEndOperator();
            final int strength = getMaxFunctionStrength() + 1;
            operators.add(new Operator(name, Operator.Type.FUNCTION_START, strength, true, false,
                    end, -1));
        }
    }

    private int getMaxFunctionStrength() {
        int out = 53000;
        for (Operator o : operators) {
            if (o.getType() == Operator.Type.FUNCTION_START && o.getStrength() > out) {
                out = o.getStrength();
            }
        }
        return out;
    }

    private Operator getFunctionEndOperator() {
        for (Operator o : operators) {
            if (o.getType() == Operator.Type.GROUPING_END) {
                return o;
            }
        }
        return null;
    }

    private Collection<Operator> setupOperators() {
        Set<Operator> out = new TreeSet<>((m1, m2) -> m1.getStrength() - m2.getStrength());

        final Operator bracketEnd = new Operator(")", Operator.Type.GROUPING_END, 55001);
        out.add(bracketEnd);
        out.add(new Operator("(", Operator.Type.GROUPING_START, 55000, false, false, bracketEnd));

        final Operator arrayEnd = new Operator("]", Operator.Type.ARRAY_END, 55101);
        out.add(arrayEnd);

        out.add(new Operator("[", Operator.Type.ARRAY_START, 55100, false, false, arrayEnd));
        out.add(new Operator(",", Operator.Type.ARRAY_DELIMITER, 55200));

        out.add(new Operator("&&", Operator.Type.MIDDLE, 5000));
        out.add(new Operator("||", Operator.Type.MIDDLE, 5001));

        out.add(new Operator("==", Operator.Type.MIDDLE, 5100));
        out.add(new Operator("<=", Operator.Type.MIDDLE, 5101));
        out.add(new Operator(">=", Operator.Type.MIDDLE, 5102));
        out.add(new Operator("<", Operator.Type.MIDDLE, 5103));
        out.add(new Operator(">", Operator.Type.MIDDLE, 5104));
        out.add(new Operator("!=", Operator.Type.MIDDLE, 5105));

        out.add(new Operator("+", 10100, 50000));
        out.add(new Operator("-", 10200, 50001));
        out.add(new Operator("*", Operator.Type.MIDDLE, 10400));
        out.add(new Operator("/", Operator.Type.MIDDLE, 10500));
        out.add(new Operator("%", Operator.Type.MIDDLE, 10600));
        out.add(new Operator(" in ", Operator.Type.MIDDLE, 11000, true, false, null));

        out.add(new Operator("!", Operator.Type.PRE, 20100));

        out.add(new Operator(LITERAL_QUOTE_CHAR, Operator.Type.STRING_SIGN, 50010));

        return out;
    }

    Node parse(String expression) {
        final List<Match> matches = operatorMatcher.matches(expression);
        final Range range = new Range(expression);

        final Node out = createNode(matches, range);

        if (containsForbiddenSymbolNames(out)) {
            throw new MatchExpressionParseException(
                    "Names in match expression must only contain alphanumeric characters, " +
                            "\"$\", and \"_\"");
        }

        return out;
    }

    private boolean containsForbiddenSymbolNames(Node node) {
        if (node.getOperatorType() == Operator.Type.FUNCTION_START) {
            if (!functionName.matcher(node.getOperatorSymbol()).matches()) {
                return false;
            }
        } else if (!node.isLiteralString() && !node.isNumberFormatted()
                && node.getOperatorType() == null &&
                symbolForbiddenChars.matcher(node.getExpression()).find()) {
            return true;
        }
        boolean returnVal = false;
        if (node.getSubNodes() != null) {
            for (Node n : node.getSubNodes()) {
                if (containsForbiddenSymbolNames(n)) {
                    returnVal = true;
                }
            }
        }
        return returnVal;
    }

    private Node createNode(Range expression) {
        return new Node(expression);
    }

    private Node createGroupNode(List<Match> matches, Range expression) {
        List<Match> regionMatches = matches.subList(1, matches.size() - 1);
        Range regionRange = expression.getRegion(matches.get(0),
                matches.get(matches.size() - 1));
        Node content = createNode(regionMatches, regionRange);
        return new Node(matches.get(0).getOperator(), expression, content);
    }

    private Node createPreNode(List<Match> matches, Range expression) {
        List<Match> rightMatches = matches.subList(1, matches.size());
        Range rightRange = expression.getRightSplit(matches.get(0));
        Node right = createNode(rightMatches, rightRange);
        return new Node(matches.get(0).getOperator(), expression, right);
    }

    private Node createMiddleNode(List<Match> matches, Match root, Range expression) {
        int splitIndex = matches.indexOf(root);
        if (root.getIndex() == expression.getStart()) {
            return createPreNode(matches, expression);
        }

        List<Match> leftMatches = matches.subList(0, splitIndex);
        List<Match> rightMatches = matches.subList(splitIndex + 1, matches.size());
        Range leftRange = expression.getLeftSplit(root);
        Range rightRange = expression.getRightSplit(root);
        Node left = createNode(leftMatches, leftRange);
        Node right = createNode(rightMatches, rightRange);
        return new Node(root.getOperator(), expression, left, right);
    }

    private Node createArrayNode(List<Match> matches, Range expression) {
        Range range = expression.getRightSplit(matches.get(0));
        List<Node> items = getArray(matches.subList(1, matches.size()), range);
        return new Node(items, matches.get(0).getOperator(), expression);
    }

    private String cleanStringFromEscapeChars(Range range) {
        return range.getExpression().replace(ESCAPE_CHAR + LITERAL_QUOTE_CHAR, LITERAL_QUOTE_CHAR);
    }

    private Node createStringNode(List<Match> matches, Range expression) {
        String string = cleanStringFromEscapeChars(
                expression.getRegion(matches.get(0), matches.get(matches.size() - 1)));
        Node stringNode = new Node(string);
        return new Node(matches.get(0).getOperator(), expression, stringNode);
    }

    private Node createNode(List<Match> matches, Range expression) {
        if (matches.isEmpty()) {
            return createNode(expression);
        }

        final Match root = getRootMatch(matches);

        switch (root.getOperator().getType()) {
        case GROUPING_START:
            return createGroupNode(matches, expression);
        case PRE:
            return createPreNode(matches, expression);
        case MIDDLE:
            return createMiddleNode(matches, root, expression);
        case ARRAY_START:
            return createArrayNode(matches, expression);
        case FUNCTION_START:
            return createFunctionNode(matches, expression);
        case STRING_SIGN:
            return createStringNode(matches, expression);
        default:
            return null;
        }
    }

    private Node createFunctionNode(List<Match> matches, Range expression) {
        Range range = expression.getRightSplit(matches.get(1));
        List<Node> items = getArray(matches.subList(2, matches.size()), range);
        return new Node(items, matches.get(0).getOperator(), expression);
    }

    private List<Node> getArray(List<Match> matches, Range expression) {
        final List<Node> out = new ArrayList<>();
        int bracketCount = 0;
        Range current = expression;
        int currentIndex = -1;
        boolean inString = false;
        for (int i = 0; i < matches.size(); i++) {
            final Match match = matches.get(i);
            if (match.getOperator().getType() == Operator.Type.STRING_SIGN) {
                inString = !inString;
            } else if (inString) {
                continue;
            }
            if (bracketCount == 0
                    && match.getOperator().getType() == Operator.Type.ARRAY_DELIMITER) {
                out.add(createNode(matches.subList(currentIndex + 1, i),
                        current.getLeftSplit(match)));
                current = current.getRightSplit(match);
                currentIndex = i;
            } else if (match.getOperator().getType() == Operator.Type.GROUPING_START
                    || match.getOperator().getType() == Operator.Type.ARRAY_START) {
                bracketCount++;
            } else if (match.getOperator().getType() == Operator.Type.GROUPING_END
                    || match.getOperator().getType() == Operator.Type.ARRAY_END) {
                bracketCount--;
                if (bracketCount < 0) {
                    out.add(createNode(matches.subList(currentIndex + 1, i),
                            current.getLeftSplit(match)));
                    currentIndex = i;
                }
            }
        }
        return out;
    }

    private Match getRootMatch(List<Match> matches) {
        Match root = null;
        int groupCount = 0;
        int arrayCount = 0;
        boolean isString = false;

        for (Match m : matches) {
            if (isString && m.getOperator().getType() != Operator.Type.STRING_SIGN) {
                continue;
            }
            switch (m.getOperator().getType()) {
            case STRING_SIGN:
                isString = !isString;
                if (root == null && isString) {
                    root = m;
                }
                break;
            case GROUPING_START:
                if (root == null && groupCount == 0) {
                    root = m;
                }
                groupCount++;
                break;
            case GROUPING_END:
                groupCount--;
                break;
            case ARRAY_START:
                if (root == null && arrayCount == 0) {
                    root = m;
                }
                arrayCount++;
                break;
            case ARRAY_END:
                arrayCount--;
                break;
            case FUNCTION_START:
                if (root == null && groupCount == 0) {
                    root = m;
                }
                break;
            case ARRAY_DELIMITER:
                break;
            default:
                if (groupCount > 0 || arrayCount > 0) {
                    break;
                }
                if (root == null) {
                    root = m;
                } else if (root.getOperator().getStrength() > m.getOperator().getStrength()) {
                    root = m;
                }
                break;
            }
        }
        return root;
    }
}
