/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.Value;
import com.king.tratt.tdl.CheckPoint;

final class CheckPointMatcher {
    private final MatcherParser matcherParser;
    private final Environment env;
    private final EventMetaData eventMetaData;
    private final List<Matcher> matchers;
    private final List<Matcher> validators;
    private final Map<String, Value> valuesToStore;
    final CheckPoint checkPoint;
    final int seqIndex;
    final int cpIndex;

    CheckPointMatcher(int seqIndex, int cpIndex, CheckPoint checkPoint, Environment env,
            MatcherParser matcherParser, StartedEventProcessor started,
            Map<String, Value> valuesToStore) {
        this.seqIndex = seqIndex;
        this.cpIndex = cpIndex;
        this.checkPoint = checkPoint;
        this.env = env;
        this.matcherParser = matcherParser;
        this.valuesToStore = valuesToStore;
        this.eventMetaData = started.metadataFactory.getEventMetaData(checkPoint.getEventType());
        this.matchers = createMatchers();
        this.validators = createValidators();
    }

    private List<Matcher> createMatchers() {
        List<Matcher> result = new ArrayList<>();
        Matcher eventMatcher = matcherParser.createEventTypeMatcher(eventMetaData);
        result.add(eventMatcher);
        Matcher matcher = matcherParser.parseMatcher(eventMetaData, checkPoint.getMatch(), env);
        if (matcher != null) { // TODO remove null check?
            result.add(matcher);
        }
        return result;
    }

    private List<Matcher> createValidators() {
        final String validate = checkPoint.getValidate();
        if (validate == null || validate.isEmpty()) {
            return Collections.emptyList();
        }
        List<Matcher> result = new ArrayList<>();
        Matcher validator = matcherParser.parseMatcher(eventMetaData, validate, env);
        if (validator != null) { // TODO remove null check?
            result.add(validator);
        }
        return result;
    }

    public String getEventId() {
        return eventMetaData.getId();
    }

    public boolean matches(Event event, Context context) {
        return matches(matchers, event, context);
    }

    public boolean isValid(Event event, Context context) {
        return matches(validators, event, context);
    }

    private boolean matches(List<Matcher> matchers, Event event, Context context) {
        for (Matcher m : matchers) {
            if (!m.matches(event, context)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSufficientMatchContext(Context context) {
        return hasSufficentContext(matchers, context);
    }

    public boolean hasSufficientValidContext(Context context) {
        return hasSufficentContext(validators, context);
    }

    private boolean hasSufficentContext(List<Matcher> matchers, Context context) {
        for (Matcher m : matchers) {
            if (!m.hasSufficientContext(context)) {
                return false;
            }
        }
        return true;
    }

    public void updateContext(Event event, Context context) {
        for (Entry<String, Value> e : valuesToStore.entrySet()) {
            Object value = e.getValue().get(event, context);
            context.set(e.getKey(), value);
        }
    }

    @Override
    public String toString() {
        return checkPoint.toString();
    }

    public String getDebugString(Event event, Context context) {
        if (validators.isEmpty()) {
            return "";
        }
        String indent = "      ";
        Matcher matcher = validators.get(0);
        String message = matcher.toDebugString(event, context);
        //
        // remove "source:", white spaces and brackets, then split on "&&"
        message = message
                .replaceAll("source:", "")
                .replaceAll("\\s*\\(\\s*", "")
                .replaceAll("\\s*\\)\\s*", "")
                .replaceAll("\\s*&&\\s*", " &&\n" + indent);
        return indent + message;
    }
}
