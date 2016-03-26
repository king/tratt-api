package com.king.tratt;

import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonParseException;
import com.king.tratt.VariableParser.NameValue;
import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.CheckPoint;
import com.king.tratt.tdl.Sequence;
import com.king.tratt.tdl.Tdl;

class TdlValidator {

    private final EventMetaDataFactory metaDataFactory;
    private List<String> errors = new ArrayList<>();
    private MatcherParser matcherParser;
    private int numParseErrors = 0;
    private List<TdlFileFieldErrorDescriptor> fieldErrorDescriptions = new ArrayList<>();
    private ValueFactory valueFactory;

    TdlValidator(ValueFactory valueFactory, EventMetaDataFactory metaDataProvider, final Tdl tdl) {
        this.valueFactory = valueFactory;
        this.metaDataFactory = metaDataProvider;
        this.matcherParser = new MatcherParser(valueFactory);
        validateTdl(tdl);
    }

    private void validateTdl(final Tdl tdl) {
        String nodePath = "root";

        try {
            Map<String, String> variablesMap = new HashMap<String, String>();
            List<String> tdlVariables = tdl.getVariables();
            if (tdlVariables != null) {
                for (int i = 0; i < tdlVariables.size(); i++) {
                    String nameValueString = tdlVariables.get(i);
                    validateVariable(variablesMap, nameValueString, nodePath + "[variables][" + i + "]");
                }
            }

            List<Sequence> sequences = tdl.getSequences();
            for (int i = 0; i < sequences.size(); i++) {
                Sequence s = sequences.get(i);
                Environment env = new Environment(variablesMap);
                validateSequence(env, s, nodePath + "[sequences][" + i + "]");
            }

        } catch (JsonParseException e) {
            errors.add("Invalid Json. (Please check with JSON lint.)");
            return;
        }
    }
    private void validateVariable(Map<String, String> variablesMap, String variableExpression, String nodePath) {
        try {
            NameValue nameValue = VariableParser.parse("$", variableExpression);
            variablesMap.put(nameValue.name, nameValue.value);
        } catch (Exception e) {
            addNodeError("Invalid variable set expression \"" + variableExpression + "\". (" + e.getMessage() + ")",
                    nodePath);
        }
    }

    private void validateSequence(Environment env, Sequence sequence, String nodePath) {
        final List<String> validTypes = Arrays.asList("funnel", "container", "unwanted");
        if (!validTypes.contains(sequence.getType().toString().toLowerCase())) {
            addNodeError("Invalid sequence type \"" + sequence.getType() + "\". Valid types are " + validTypes,
                    nodePath + "[type]");
        }

        try {
            Duration.parse(sequence.getSequenceMaxTime());
        } catch (Exception e) {
            addNodeError(
                    "Invalid time format \"" + sequence.getSequenceMaxTime()
                            + "\". (Use ISO 8601 durations, see http://en.wikipedia.org/wiki/ISO_8601#Durations)",
                    nodePath + "[sequenceMaxTime]");
        }

        List<CheckPoint> checkPoints = sequence.getCheckPoints();
        SetterToValueMapper mapper = new SetterToValueMapper(valueFactory);
        env.sequenceVariables.putAll(
                checkPoints.stream().flatMap(mapper::getValues)
                        .collect(toMap(Entry::getKey, Entry::getValue)));
        for (int i = 0; i < checkPoints.size(); i++) {
            CheckPoint cp = checkPoints.get(i);
            validateCheckPoint(env, cp, nodePath + "[checkPoints][" + i + "]");
        }
    }


    private void validateCheckPoint(Environment env, CheckPoint checkPoint, String nodePath) {
        EventMetaData eventMetaData = metaDataFactory.getEventMetaData(checkPoint.getEventType());
        if (eventMetaData == null) {
            addNodeError(
                    "No EventType with name \""
                            + checkPoint.getEventType()
                            + "\" (Must be an EventType from http://ivy.dev.midasplayer.com/repository/king/king_constants/latest/metadata/EventType.html)",
                            nodePath + "[eventType]");
            return;
        }

        try {
            matcherParser.parseMatcher(eventMetaData, checkPoint.getMatch(), env);
        } catch (Exception e) {
            addNodeError("Bad matcher \"" + checkPoint.getMatch() + "\"" + ". Error info: " + e.getMessage(), nodePath
                    + "[match]");
        }

        try {
            matcherParser.parseMatcher(eventMetaData, checkPoint.getValidate(), env);
        } catch (Exception e) {
            addNodeError("Bad validate \"" + checkPoint.getValidate() + "\"" + ". Error info: " + e.getMessage(),
                    nodePath + "[validate]");
        }

        List<String> setters = checkPoint.getSet();
        if (setters != null) {
            for (int i = 0; i < setters.size(); i++) {
                String s = setters.get(i);
                validateSetter(eventMetaData, s, env, nodePath + "[set][" + i + "]");
            }
        }
    }

    private void validateSetter(EventMetaData eventMetaData, String setExpression, Environment env,
            String nodePath) {
        try {
            NameValue nameValue = VariableParser.parse(setExpression);
            SetterToValueMapper m = new SetterToValueMapper(valueFactory);
            Value value = m.getValue(eventMetaData.getName(), nameValue.value);
            env.sequenceVariables.put(nameValue.name, value);
        } catch (Exception e) {
            addNodeError("Invalid sequence-local variable set expression \"" + setExpression + "\". (" + e.getMessage()
                    + ")", nodePath);
        }
    }

    private void addNodeError(String errorDescription, String nodePath) {
        numParseErrors++;
        fieldErrorDescriptions.add(new TdlFileFieldErrorDescriptor(errorDescription, nodePath));
    }

    String getError() {
        String ret = "";
        if (numParseErrors > 0) {
            ret += "" + numParseErrors + " TDL parse errors.\n";
        }
        if (errors.isEmpty() && ret.isEmpty()) {
            return null;
        }
        for (String e : errors) {
            ret += e;
        }
        return ret;
    }

    List<TdlFileFieldErrorDescriptor> getFieldErrorDescriptors() {
        return fieldErrorDescriptions;
    }

}
