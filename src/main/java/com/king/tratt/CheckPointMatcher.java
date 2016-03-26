package com.king.tratt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            MatcherParser matcherParser, StartedEventProcessor started, Map<String, Value> valuesToStore) {
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
        List<Matcher> matchers = new ArrayList<>();
        Matcher eventMatcher = matcherParser.createEventTypeMatcher(eventMetaData);
        matchers.add(eventMatcher);
        Matcher matcher = matcherParser.parseMatcher(eventMetaData, checkPoint.getMatch(), env);
        if (matcher != null) { // TODO remove null check?
            matchers.add(matcher);
        }
        return matchers;
    }

    private List<Matcher> createValidators() {
        final String validate = checkPoint.getValidate();
        if (validate == null || validate.isEmpty()) {
            return Collections.emptyList();
        }
        List<Matcher> validators = new ArrayList<>();
        Matcher validator = matcherParser.parseMatcher(eventMetaData, validate, env);
        if (validator != null) { // TODO remove null check?
            validators.add(validator);
        }
        return validators;
    }

    public long getEventId() {
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
        for (String key : valuesToStore.keySet()) {
            Object value = valuesToStore.get(key).get(event, context);
            context.set(key, value);
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
        String INDENT = "      ";
        Matcher matcher = validators.get(0);
        String message = matcher.toDebugString(event, context);
        //
        // remove "source:", white spaces and brackets, then split on "&&"
        message = message
                .replaceAll("source:", "")
                .replaceAll("\\s*\\(\\s*", "")
                .replaceAll("\\s*\\)\\s*", "")
                .replaceAll("\\s*&&\\s*", " &&\n" + INDENT);
        return INDENT + message;
    }
}
