// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static com.king.tratt.FunctionFactory.VAR_ARG;
import static com.king.tratt.Tratt.util;
import static com.king.tratt.Tratt.values;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;

class MatcherParser {

    private final TdlNodeParser tdlNodeParser;
    private final ValueFactory valueFactory;
    private final FunctionFactoryProvider functionProvider;

    MatcherParser(ValueFactory valueFactory) {
        this.tdlNodeParser = new TdlNodeParser();
        this.valueFactory = valueFactory;
        this.functionProvider = new FunctionFactoryProvider();
        tdlNodeParser.addFunctions(functionProvider.getFunctionNames());
    }

    Matcher createEventTypeMatcher(EventMetaData eventMetaData) {
        Value left = values.constantString(eventMetaData.getId());
        return Matcher.equalTo(left, values.eventId());
    }

    Matcher parseMatcher(EventMetaData eventMetaData, String expression, Environment env) {
        if (expression == null || eventMetaData == null) {
            return null;
        }
        if (expression.length() == 0) {
            return null;
        }

        Node node = tdlNodeParser.parse(expression);
        return buildMatcher(eventMetaData, node, env);
    }

    private Matcher buildMatcher(EventMetaData eventMetaData, Node node, Environment env) {
        if (node.getOperatorType() == null) {
            return buildIntBooleanMatcher(eventMetaData, node, env);
        }
        switch (node.getOperatorType()) {
        case PRE:
            return buildPreMatcher(eventMetaData, node, env);
        case MIDDLE:
            return buildMiddleMatcher(eventMetaData, node, env);
        case GROUPING_START:
            return buildMatcher(eventMetaData, node.getNode(0), env);
        case FUNCTION_START:
            return buildFunctionMatcher(eventMetaData, node, env);
        default:
            return null;
        }
    }

    private Matcher buildIntBooleanMatcher(EventMetaData eventMetaData, Node node,
            Environment env) {
        Value value = getValue(eventMetaData, node, env);
        return Matcher.intBoolean(value);
    }

    private Matcher buildFunctionMatcher(EventMetaData eventMetaData, Node node, Environment env) {
        List<Value> arguments = new ArrayList<>();
        for (Node n : node.getSubNodes()) {
            arguments.add(getValue(eventMetaData, n, env));
        }
        Value out = createFunctionValue(node.getOperatorSymbol(), arguments);
        return Matcher.functionMatcher(out);
    }

    private Value createFunctionValue(String functionName, List<Value> arguments) {
        FunctionFactory func = functionProvider.get(functionName);
        if (func.getNumberOfArguments() == VAR_ARG) {
            // do nothing. function is var arg.
        } else if (func.getNumberOfArguments() != arguments.size()) {
            String message = "Function %s:%s is called with incorrect number of arguments: %s!";
            throw new IllegalStateException(String.format(message,
                    func.getName(), func.getNumberOfArguments(), arguments.size()));
        }
        return func.create(arguments);
    }

    private Matcher buildPreMatcher(EventMetaData eventType, Node node, Environment env) {
        if (!"!".equals(node.getOperatorSymbol())) {
            return null;
        }

        Matcher matcher = buildMatcher(eventType, node.getNode(0), env);
        if (matcher == null) {
            throw new IllegalArgumentException();
        }

        return Matcher.not(matcher);
    }

    private Matcher buildAndOrMatcher(EventMetaData eventMetaData, Node node, Environment env) {
        Matcher left = buildMatcher(eventMetaData, node.getNode(0), env);
        Matcher right = buildMatcher(eventMetaData, node.getNode(1), env);
        if (left == null || right == null) {
            throw new IllegalArgumentException("Bad syntax near: " + node.getCurrentString());
        }
        switch (node.getOperatorSymbol()) {
        case "&&":
            return Matcher.and(left, right);
        case "||":
            return Matcher.or(left, right);
        default:
            return null;
        }
    }

    private Value getValue(EventMetaData eventMetaData, Node node, Environment env) {
        if (node.getOperatorType() != null) {
            switch (node.getOperatorSymbol()) {
            case "'":
                String nodeValue = node.getNode(0).getExpression();
                return values.constantString(nodeValue);
            case "%":
                Value value = getValue(eventMetaData, node.getNode(0), env);
                Value modulus = getValue(eventMetaData, node.getNode(1), env);
                return values.modulus(value, modulus);
            case "+": {
                Value left = getValue(eventMetaData, node.getNode(0), env);
                Value right = getValue(eventMetaData, node.getNode(1), env);
                return values.sum(left, right);
            }
            case "-": {
                Value left;
                Value right;
                // handle "a-b" as well as "-a"
                if (node.getSubNodes().size() > 1) {
                    left = getValue(eventMetaData, node.getNode(0), env);
                    right = getValue(eventMetaData, node.getNode(1), env);
                } else {
                    left = values.constantLong(0);
                    right = getValue(eventMetaData, node.getNode(0), env);
                }
                return values.subtract(left, right);
            }
            case "*": {
                Value left = getValue(eventMetaData, node.getNode(0), env);
                Value right = getValue(eventMetaData, node.getNode(1), env);
                return values.multiply(left, right);
            }
            case "/": {
                Value left = getValue(eventMetaData, node.getNode(0), env);
                Value right = getValue(eventMetaData, node.getNode(1), env);
                return values.divide(left, right);
            }
            case "(": {
                return getValue(eventMetaData, node.getNode(0), env);
            }
            default: {
                if (node.getOperatorType() == Operator.Type.FUNCTION_START) {
                    List<Value> arguments = new ArrayList<>();
                    for (Node n : node.getSubNodes()) {
                        arguments.add(getValue(eventMetaData, n, env));
                    }
                    return createFunctionValue(node.getOperatorSymbol(), arguments);
                }
            }
            }
        }

        Value valueFromFactory;
        String nodeValue = node.getExpression();
        String eventName = eventMetaData.getName();
        if (env.sequenceVariables.containsKey(nodeValue)) {
            return values.context(nodeValue);
        } else if (env.tdlVariables.containsKey(nodeValue)) {
            return values.constant(env.tdlVariables.get(nodeValue));
        } else if (util.isLong(nodeValue)) {
            return values.constantLong(parseLong(nodeValue));
        } else if (util.isBoolean(nodeValue)) {
            return values.constantBoolean(parseBoolean(nodeValue));
        } else if ((valueFromFactory = valueFactory.getValue(eventName, nodeValue)) != null) {
            return valueFromFactory;
        }
        String message = "Faulty value is '%s', where possible problems could be: "
                + "No field with that name defined in  '%s' event, or "
                + "No variable defined/set in TDL with that name, or "
                + "string value not quoted with single quotes.";
        throw new IllegalStateException(format(message, nodeValue, eventName));

    }

    private Matcher buildCompareMatcher(EventMetaData eventMetaData, Node node, Environment env) {
        Value left = getValue(eventMetaData, node.getNode(0), env);
        Value right = getValue(eventMetaData, node.getNode(1), env);

        switch (node.getOperatorSymbol()) {
        case "==":
            return Matcher.equalTo(left, right);
        case "!=":
            return Matcher.notEqual(left, right);
        case "<":
            return Matcher.lessThan(left, right);
        case "<=":
            return Matcher.lessThanOrEqual(left, right);
        case ">":
            return Matcher.greaterThan(left, right);
        case ">=":
            return Matcher.greaterThanOrEqual(left, right);
        default:
            return null;
        }
    }

    private Matcher buildMiddleMatcher(EventMetaData eventType, Node node, Environment env) {
        switch (node.getOperatorSymbol()) {
        case "&&":
        case "||":
            return buildAndOrMatcher(eventType, node, env);
        case "==":
        case ">=":
        case "<=":
        case "<":
        case ">":
        case "!=":
            return buildCompareMatcher(eventType, node, env);
        default:
            return null;
        }
    }

}
