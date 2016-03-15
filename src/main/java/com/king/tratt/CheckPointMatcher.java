package com.king.tratt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.Value;
import com.king.tratt.tdl.CheckPoint;

final class CheckPointMatcher<E extends Event> {
    private final MatcherParser<E> matcherParser;
    private final Environment<E> env;
    private final EventMetaData eventMetaData;
    private final ContextImp context;
    private final List<Matcher<E>> matchers;
    private final List<Matcher<E>> validators;
    private final Map<String, Value<E>> valuesToStore;
    final CheckPoint checkPoint;
    final int seqIndex;
    final int cpIndex;

    CheckPointMatcher(int seqIndex, int cpIndex, CheckPoint checkPoint, Environment<E> env,
            MatcherParser<E> matcherParser, StartedEventProcessor<E> started, ContextImp context,
            Map<String, Value<E>> valuesToStore) {
        this.seqIndex = seqIndex;
        this.cpIndex = cpIndex;
        this.checkPoint = checkPoint;
        this.env = env;
        this.matcherParser = matcherParser;
        this.context = context;
        this.valuesToStore = valuesToStore;
        this.eventMetaData = started.metadataFactory.getEventMetaData(checkPoint.getEventType());
        this.matchers = createMatchers();
        this.validators = createValidators();
    }

    private List<Matcher<E>> createMatchers() {
        List<Matcher<E>> matchers = new ArrayList<>();
        Matcher<E> eventMatcher = matcherParser.createEventTypeMatcher(eventMetaData);
        matchers.add(eventMatcher);
        Matcher<E> matcher = matcherParser.parseMatcher(eventMetaData, checkPoint.getMatch(), env);
        if (matcher != null) {
            matchers.add(matcher);
        }
        return matchers;
    }

    private List<Matcher<E>> createValidators() {
        final String validate = checkPoint.getValidate();
        if (validate == null || validate.isEmpty()) {
            return Collections.emptyList();
        }
        List<Matcher<E>> validators = new ArrayList<>();
        Matcher<E> validator = matcherParser.parseMatcher(eventMetaData, validate, env);
        if (validator != null) {
            validators.add(validator);
        }
        return validators;
    }

    public long getEventId() {
        return eventMetaData.getId();
    }

    public boolean matches(E event) {
        return matches(matchers, event);
        // for (Matcher<E> m : matchers) {
        // if (!m.matches(event, context)) {
        // return false;
        // }
        // }
        // return true;
    }

    public boolean isValid(E event) {
        return matches(validators, event);
    }

    private boolean matches(List<Matcher<E>> matchers, E event) {
        for (Matcher<E> m : matchers) {
            if (!m.matches(event, context)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSufficientMatchContext() {
        return hasSufficentContext(matchers);
    }

    public boolean hasSufficientValidContext() {
        return hasSufficentContext(validators);
    }

    private boolean hasSufficentContext(List<Matcher<E>> matchers) {
        for (Matcher<E> m : matchers) {
            if (!m.hasSufficientContext(context)) {
                return false;
            }
        }
        return true;
    }

    public void updateContext(E event) {
        for (String key : valuesToStore.keySet()) {
            Object value = valuesToStore.get(key).get(event, context);
            context.set(key, value);
        }
    }

    @Override
    public String toString() {
        return checkPoint.toString();
    }

    public String getDebugString(E event) {
        if (validators.isEmpty()) {
            return "";
        }
        String INDENT = "      ";
        Matcher<E> matcher = validators.get(0);
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
