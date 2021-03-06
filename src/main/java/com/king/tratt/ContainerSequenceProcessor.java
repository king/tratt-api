// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;

class ContainerSequenceProcessor extends SequenceProcessor {
    private final List<Memory> eventsToMatchContainer = new CopyOnWriteArrayList<>();
    private final List<Memory> eventsToValidateContainer = new CopyOnWriteArrayList<>();
    private Context context;
    private Map<String, List<CheckPointMatcher>> checkPointMatchers;
    private SequenceProcessorHelper helper;
    private long maxTimeMillis;
    private Long endTimeMillis;

    /*
     * State definition fields
     */
    private final EventState EXIT_PROCESSING = memory -> {
        throw new ExitException();
    };
    private final EventState BREAK_STATE_MACHINE_LOOP = memory -> {
        throw new UnsupportedOperationException();
    };

    private final EventState MATCH_CHECKER_STATE_THAT_BREAKS_LOOP = memory -> {
        return matchCheckerState(memory, BREAK_STATE_MACHINE_LOOP);
    };
    private final EventState MATCH_CHECKER_STATE_THAT_THROWS = memory -> {
        return matchCheckerState(memory, EXIT_PROCESSING);
    };

    EventState matchCheckerState(Memory memory, EventState def) {
        for (CheckPointMatcher cpMatcher : checkPointMatchers.get(memory.eventId)) {
            if (cpMatcher.hasSufficientMatchContext(context)) {
                boolean matches = cpMatcher.matches(memory.event, context);
                if (matches) {
                    memory.cpMatcher = cpMatcher;
                    return MATCHED_STATE;
                }
            } else {
                eventsToMatchContainer.add(memory);
                return BREAK_STATE_MACHINE_LOOP;
            }
        }
        return def;
    }

    private final EventState VALID_STATE = memory -> {
        removeCheckPoint(memory);
        helper.notifyCheckPointSuccess(memory.event, memory.cpMatcher, context);
        return BREAK_STATE_MACHINE_LOOP;
    };

    private final EventState INVALID_STATE = memory -> {
        removeCheckPoint(memory);
        helper.notifyCheckPointFailure(memory.event, memory.cpMatcher, context);
        return BREAK_STATE_MACHINE_LOOP;
    };

    private void removeCheckPoint(Memory memory) {
        checkPointMatchers.get(memory.eventId).remove(memory.cpMatcher);
        if (checkPointMatchers.get(memory.eventId).isEmpty()) {
            checkPointMatchers.remove(memory.eventId);
        }
    }

    private final EventState VALID_CHECKER_STATE = memory -> {
        CheckPointMatcher cpMatcher = memory.cpMatcher;
        if (cpMatcher.hasSufficientValidContext(context)) {
            if (cpMatcher.isValid(memory.event, context)) {
                return VALID_STATE;
            } else {
                return INVALID_STATE;
            }
        } else {
            eventsToValidateContainer.add(memory);
            return BREAK_STATE_MACHINE_LOOP;
        }
    };

    private final EventState MATCHED_STATE = memory -> {
        memory.cpMatcher.updateContext(memory.event, context);
        if (isFirstMatchInSequence()) {
            endTimeMillis = memory.event.getTimestampMillis() + maxTimeMillis;
            helper.notifySequenceStart(context);
        }
        helper.notifyCheckPointMatch(memory.event, memory.cpMatcher, context);
        return VALID_CHECKER_STATE;
    };

    private boolean isFirstMatchInSequence() {
        return endTimeMillis == null;
    }

    @Override
    public void beforeStartImp(SequenceProcessorHelper helper) {
        this.helper = helper;
        maxTimeMillis = helper.getMaxTimeMillis();
        resetSequence();
    }

    private void resetSequence() {
        checkPointMatchers = helper.getCheckPointMatchers().stream()
                .collect(groupingBy(CheckPointMatcher::getEventId));
        context = helper.newContext();
        endTimeMillis = null;
        eventsToMatchContainer.clear();
        eventsToValidateContainer.clear();
    }

    @Override
    public void onTimeoutImp() {
        for (Entry<String, List<CheckPointMatcher>> entry : checkPointMatchers.entrySet()) {
            for (CheckPointMatcher cpMatcher : entry.getValue()) {
                helper.notifyCheckPointTimeout(cpMatcher, context);
            }
        }
        helper.notifySequenceTimeout(context);
        helper.notifySequenceEnd(context);
        resetSequence();
    }

    private static class Memory {
        final Event event;
        final String eventId;
        CheckPointMatcher cpMatcher;

        Memory(Event event) {
            this.event = event;
            eventId = event.getId();
        }

        @Override
        public String toString() {
            return event.toString();
        }
    }

    @SuppressWarnings("serial")
    private static class ExitException extends RuntimeException {
    }

    @FunctionalInterface
    private interface EventState {
        EventState next(Memory memory);
    }

    @Override
    public void processImp(Event event) {
        try {
            if (hasSequenceMaxtimePassed(event)) {
                onTimeoutImp();
            }
            if (!checkPointMatchers.containsKey(event.getId())) {
                return;
            }
            runStateMachine(MATCH_CHECKER_STATE_THAT_THROWS, new Memory(event));
            washContainer(eventsToMatchContainer, MATCH_CHECKER_STATE_THAT_BREAKS_LOOP);
            washContainer(eventsToValidateContainer, VALID_CHECKER_STATE);
            if (isReady()) {
                helper.notifySequenceEnd(context);
                resetSequence();
            }
        } catch (ExitException e) {
            return;
        }
    }

    private boolean hasSequenceMaxtimePassed(Event event) {
        if (endTimeMillis == null) {
            return false;
        }
        return event.getTimestampMillis() > endTimeMillis;
    }

    private void runStateMachine(EventState state, final Memory m) {
        if (state == BREAK_STATE_MACHINE_LOOP) {
            return;
        }
        runStateMachine(state.next(m), m);
    }

    private void washContainer(List<Memory> container, EventState eventState) {
        for (Memory memory : container) {
            container.remove(memory);
            runStateMachine(eventState, memory);
        }
    }

    private boolean isReady() {
        return checkPointMatchers.isEmpty();
    }
}
