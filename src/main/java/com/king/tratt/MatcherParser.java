/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt;

import static com.king.tratt.FunctionFactory.VAR_ARG;
import static com.king.tratt.Tratt.util;
import static com.king.tratt.Tratt.values;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;

class MatcherParser<E extends Event> {

    private final TdlNodeParser tdlNodeParser;
    private final ValueFactory<E> valueFactory;
    private final FunctionFactoryProvider<E> functionProvider;

    MatcherParser(ValueFactory<E> valueFactory) {
        this.tdlNodeParser = new TdlNodeParser();
        this.valueFactory = valueFactory;
        this.functionProvider = new FunctionFactoryProvider<>();
        tdlNodeParser.addFunctions(functionProvider.getFunctionNames());
    }

    Matcher<E> createEventTypeMatcher(EventMetaData eventMetaData) {
        final Value<E> left = values.constantLong(eventMetaData.getId());
        return Matcher.equal(left, values.eventId());
    }

    Matcher<E> parseMatcher(EventMetaData eventMetaData, String expression, Environment<E> env) {
        if (expression == null || eventMetaData == null) {
            return null;
        }
        if (expression.length() == 0) {
            return null;
        }

        final Node node = tdlNodeParser.parse(expression);
        final Matcher<E> matcher = buildMatcher(eventMetaData, node, env);

        return matcher;
    }

    private Matcher<E> buildMatcher(EventMetaData eventMetaData, Node node, Environment<E> env) {
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

    private Matcher<E> buildIntBooleanMatcher(EventMetaData eventMetaData, Node node, Environment<E> env) {
        final Value<E> value = getValue(eventMetaData, node, env);
        return Matcher.intBoolean(value);
    }

    private Matcher<E> buildFunctionMatcher(EventMetaData eventMetaData, Node node, Environment<E> env) {
        final List<Value<E>> arguments = new ArrayList<>();
        for (Node n : node.getSubNodes()) {
            arguments.add(getValue(eventMetaData, n, env));
        }
        Value<E> out = createFunctionValue(node.getOperatorSymbol(), arguments);
        return Matcher.functionMatcher(out);
    }

    private Value<E> createFunctionValue(String functionName, List<Value<E>> arguments) {
        FunctionFactory<E> func = functionProvider.get(functionName);
        if (func.getNumberOfArguments() == VAR_ARG) {
            // do nothing. function is var arg.
        } else if (func.getNumberOfArguments() != arguments.size()) {
            String message = "Function %s:%s is called with incorrect number of arguments: %s!";
            throw new IllegalStateException(String.format(message,
                    func.getName(), func.getNumberOfArguments(), arguments.size()));
        }
        return func.create(arguments);
    }

    private Matcher<E> buildPreMatcher(EventMetaData eventType, Node node, Environment<E> env) {
        if (!node.getOperatorSymbol().equals("!")) {
            return null;
        }

        final Matcher<E> matcher = buildMatcher(eventType, node.getNode(0), env);
        if (matcher == null) {
            throw new IllegalArgumentException();
        }

        return Matcher.not(matcher);
    }

    private Matcher<E> buildAndOrMatcher(EventMetaData eventMetaData, Node node, Environment<E> env) {
        final Matcher<E> left = buildMatcher(eventMetaData, node.getNode(0), env);
        final Matcher<E> right = buildMatcher(eventMetaData, node.getNode(1), env);
        if (left == null || right == null) {
            throw new IllegalArgumentException("Bad syntax near: " + node.getCurrentString());
        }
        switch (node.getOperatorSymbol()) {
        case "&&":
            return Matcher.and(left, right);
        case "||":
            return Matcher.or(left, right);
        }
        return null;
    }

    private Value<E> getValue(EventMetaData eventMetaData, Node node, Environment<E> env) {
        if(node.getOperatorType() != null) {
            switch(node.getOperatorSymbol()) {
            case "'":
                final String nodeValue = node.getNode(0).getExpression();
                //                return new Constant<E>(nodeValue);
                return values.constantString(nodeValue);
            case "%":
                final Value<E> value = getValue(eventMetaData, node.getNode(0), env);
                final Value<E> modulus = getValue(eventMetaData, node.getNode(1), env);
                //                return new Modulus<E>(value, modulus);
                return values.modulus(value, modulus);
            case "+": {
                final Value<E> left = getValue(eventMetaData, node.getNode(0), env);
                final Value<E> right = getValue(eventMetaData, node.getNode(1), env);
                return values.sum(left, right); // new Plus<E>(left, right);
            }
            case "-": {
                Value<E> left;
                Value<E> right;
                // handle "a-b" as well as "-a"
                if (node.getSubNodes().size() > 1) {
                    left = getValue(eventMetaData, node.getNode(0), env);
                    right = getValue(eventMetaData, node.getNode(1), env);
                } else {
                    left = values.constantLong(0);
                    right = getValue(eventMetaData, node.getNode(0), env);
                }
                return values.subtract(left, right); // new Minus<E>(left,
                                                     // right);
            }
            case "*":{
                Value<E> left = getValue(eventMetaData, node.getNode(0), env);
                Value<E> right = getValue(eventMetaData, node.getNode(1), env);
                return values.multiply(left, right); // new Times<E>(left,
                                                     // right);
            }
            case "/":{
                Value<E> left = getValue(eventMetaData, node.getNode(0), env);
                Value<E> right = getValue(eventMetaData, node.getNode(1), env);
                return values.divide(left, right); // new Divided<E>(left,
                                                   // right);
            }
            case "(":{
                return getValue(eventMetaData, node.getNode(0), env);
            }
            default: {
                if(node.getOperatorType() == Operator.Type.FUNCTION_START) {
                    final List<Value<E>> arguments = new ArrayList<>();
                    for(Node n : node.getSubNodes()) {
                        arguments.add(getValue(eventMetaData, n, env));
                    }
                    return createFunctionValue(node.getOperatorSymbol(), arguments);
                }
            }
            }
        }

        final String nodeValue = node.getExpression();
        String eventName = eventMetaData.getName();
        final Value<E> value = valueFactory.getValue(eventName, nodeValue);
        if (value != null) {
            return value;
        } else if (env.sequenceVariables.containsKey(nodeValue)) {
            return values.context(nodeValue);
        } else if (env.tdlVariables.containsKey(nodeValue)) {
            return values.constant(env.tdlVariables.get(nodeValue));
        } else if (util.isLong(nodeValue)) {
            return values.constantLong(parseLong(nodeValue));
        } else if (util.isBoolean(nodeValue)) {
            return values.constantBoolean(parseBoolean(nodeValue));
        }
        String message = "Faulty value is '%s', where possible problems could be: "
                + "No field with that name defined in  '%s' event, or "
                + "No variable defined/set in TDL with that name, or "
                + "string value not quoted with single quotes.";
        throw new IllegalStateException(format(message, nodeValue, eventName));

    }

    private Matcher<E> buildCompareMatcher(EventMetaData eventMetaData, Node node, Environment<E> env) {
        final Value<E> left = getValue(eventMetaData, node.getNode(0), env);
        final Value<E> right = getValue(eventMetaData, node.getNode(1), env);

        switch (node.getOperatorSymbol()) {
        case "==":
            return Matcher.equal(left, right);
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
        }
        return null;
    }

    private Matcher<E> buildMiddleMatcher(EventMetaData eventType, Node node, Environment<E> env) {
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
        case " in ":
            return buildInMatcher(eventType, node, env);
        }
        return null;
    }

    private Matcher<E> buildInMatcher(EventMetaData eventMetaData, Node node, Environment<E> env) {
        final Value<E> value = getValue(eventMetaData, node.getNode(0), env);
        final List<Value<E>> values = getValueList(eventMetaData, node.getNode(1), env);
        return Matcher.in(value, values);
    }

    private List<Value<E>> getValueList(EventMetaData eventMetaData, Node node, Environment<E> env) {
        final List<Value<E>> list = new ArrayList<>();
        for (Node n : node.getSubNodes()) {
            list.add(getValue(eventMetaData, n, env));
        }
        return list;
    }

}
