/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.king.tratt.VariableParser.NameValue;
import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.CheckPoint;
import com.king.tratt.tdl.Sequence;
import com.king.tratt.tdl.Tdl;

public class TdlValidator {

    private final EventMetaDataFactory metaDataFactory;
    private List<String> errors = new ArrayList<>();
    private MatcherParser matcherParser;
    private int numParseErrors = 0;
    private List<TdlFileFieldErrorDescriptor> fieldErrorDescriptions = new ArrayList<>();
    private ValueFactory valueFactory;

    public TdlValidator(ValueFactory valueFactory, EventMetaDataFactory metaDataProvider,
            final Tdl tdl) {
        this.valueFactory = valueFactory;
        this.metaDataFactory = metaDataProvider;
        this.matcherParser = new MatcherParser(valueFactory);
        validateTdl(tdl);
    }

    private void validateTdl(final Tdl tdl) {
        String nodePath = "root";

        Map<String, String> variablesMap = new HashMap<String, String>();
        List<String> tdlVariables = tdl.getVariables();
        if (tdlVariables != null) {
            for (int i = 0; i < tdlVariables.size(); i++) {
                String nameValueString = tdlVariables.get(i);
                validateVariable(variablesMap, nameValueString,
                        nodePath + "[variables][" + i + "]");
            }
        }

        List<Sequence> sequences = tdl.getSequences();
        for (int i = 0; i < sequences.size(); i++) {
            Sequence s = sequences.get(i);
            Environment env = new Environment(variablesMap);
            validateSequence(env, s, nodePath + "[sequences][" + i + "]");
        }
    }

    private void validateVariable(Map<String, String> variablesMap, String variableExpression,
            String nodePath) {
        try {
            NameValue nameValue = VariableParser.parse("$", variableExpression);
            variablesMap.put(nameValue.name, nameValue.value);
        } catch (Exception e) {
            addNodeError("Invalid variable set expression \"" + variableExpression + "\". ("
                    + e.getMessage() + ")",
                    nodePath);
        }
    }

    private void validateSequence(Environment env, Sequence sequence, String nodePath) {
        try {
            Duration.parse(sequence.getSequenceMaxTime());
        } catch (Exception e) {
            addNodeError(
                    "Invalid time format \"" + sequence.getSequenceMaxTime()
                            + "\". (Use ISO 8601 durations, see http://en.wikipedia.org/wiki/ISO_8601#Durations)",
                    nodePath + "[sequenceMaxTime]");
        }

        /*
         * Validate "set" field in all checkPoint first, as the remaining
         * checkpoint field validations depends on env.sequenceVariables is set
         * correctly.
         */
        List<CheckPoint> checkPoints = sequence.getCheckPoints();
        for (int i = 0; i < checkPoints.size(); i++) {
            CheckPoint checkPoint = checkPoints.get(i);
            EventMetaData eventMetaData = metaDataFactory
                    .getEventMetaData(checkPoint.getEventType());
            if (eventMetaData == null) {
                addNodeError("No EventType with name \"" + checkPoint.getEventType() + "\" ",
                        nodePath + "[eventType]");
                continue;
            }
            List<String> setters = checkPoint.getSet();
            if (setters != null) {
                for (int j = 0; j < setters.size(); j++) {
                    String s = setters.get(j);
                    validateSetter(eventMetaData, s, env,
                            nodePath + "[checkPoints][" + i + "]" + "[set][" + j + "]");
                }
            }
        }

        // validate the remaining checkPoint fields.
        for (int i = 0; i < checkPoints.size(); i++) {
            CheckPoint cp = checkPoints.get(i);
            validateCheckPoint(env, cp, nodePath + "[checkPoints][" + i + "]");
        }
    }

    private void validateCheckPoint(Environment env, CheckPoint checkPoint, String nodePath) {
        EventMetaData eventMetaData = metaDataFactory.getEventMetaData(checkPoint.getEventType());
        if (eventMetaData == null) {
            // We already log faulty eventMetaData above
            return;
        }

        try {
            matcherParser.parseMatcher(eventMetaData, checkPoint.getMatch(), env);
        } catch (Exception e) {
            addNodeError("Bad matcher \"" + checkPoint.getMatch() + "\"" + ". Error info: "
                    + e.getMessage(),
                    nodePath + "[match]");
        }

        try {
            matcherParser.parseMatcher(eventMetaData, checkPoint.getValidate(), env);
        } catch (Exception e) {
            addNodeError("Bad validate \"" + checkPoint.getValidate() + "\"" + ". Error info: "
                    + e.getMessage(),
                    nodePath + "[validate]");
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
            addNodeError("Invalid sequence-local variable set expression \"" + setExpression
                    + "\". (" + e.getMessage()
                    + ")", nodePath);
        }
    }

    private void addNodeError(String errorDescription, String nodePath) {
        numParseErrors++;
        fieldErrorDescriptions.add(new TdlFileFieldErrorDescriptor(errorDescription, nodePath));
    }

    public String getError() {
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

    public List<TdlFileFieldErrorDescriptor> getFieldErrorDescriptors() {
        return fieldErrorDescriptions;
    }
}
