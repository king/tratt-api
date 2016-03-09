package com.king.tratt;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

class ContainerSequenceProcessor<E extends Event> extends SequenceProcessor<E> {
    private final List<Memory<E>> eventsToMatchContainer = new CopyOnWriteArrayList<>();
    private final List<Memory<E>> eventsToValidateContainer = new CopyOnWriteArrayList<>();
    private Map<Long, List<CheckPointMatcher<E>>> checkPointMatchers;
    private SequenceProcessorHelper<E> helper;
    private long maxTimeMillis;
    private Long endTimeMillis;

    /*
     * State definitions
     */
    private final EventState<E> EXIT_PROCESSING = memory -> {
        throw new ExitException();
    };
    private final EventState<E> BREAK_STATE_MACHINE_LOOP = memory -> {
        throw new UnsupportedOperationException();
    };
    private final EventState<E> MATCH_CHECKER_STATE_THAT_BREAKS_LOOP = memory -> {
        return matchCheckerState(memory, BREAK_STATE_MACHINE_LOOP);
    };
    private final EventState<E> MATCH_CHECKER_STATE_THAT_THROWS = memory -> {
        return matchCheckerState(memory, EXIT_PROCESSING);
    };

    EventState<E> matchCheckerState(Memory<E> memory, EventState<E> def) {
        for (CheckPointMatcher<E> cpMatcher : checkPointMatchers.get(memory.eventId)) {
            if (cpMatcher.hasSufficientMatchContext()) {
                boolean matches = cpMatcher.matches((E) memory.event);
                if (matches) {
                    memory.cpMatcher = cpMatcher;
                    return MATCHED_STATE;
                }
            } else {
                return ADD_TO_MATCH_CONTAINER_STATE;
            }
        }
        return def;
    }

    private final EventState<E> ADD_TO_MATCH_CONTAINER_STATE = memory -> {
        eventsToMatchContainer.add(memory);
        return BREAK_STATE_MACHINE_LOOP;
    };
    private final EventState<E> VALID_STATE = memory -> {
        removeCheckPoint(memory);
        // String label = memory.cpMatcher.getLabel();
        // int index = memory.cpMatcher.index;
        notifySuccess(memory.event);
        // listenerHandler.fire(of(memory.event, EMIT, index, label,
        // context));
        return BREAK_STATE_MACHINE_LOOP;
    };
    private final EventState<E> ADD_TO_VALID_CONTAINER_STATE = memory -> {
        eventsToValidateContainer.add(memory);
        return BREAK_STATE_MACHINE_LOOP;
    };
    private final EventState<E> INVALID_STATE = memory -> {
        removeCheckPoint(memory);
        // String label = memory.cpMatcher.getLabel();
        // int index = memory.cpMatcher.index;
        // Value<Event> invalidMatcher =
        // memory.cpMatcher.invalidMatcher(memory.event, context);
        notifyFailure(memory.event);
        // listenerHandler.fire(of(memory.event, INVALID, index, label,
        // context, invalidMatcher));
        return BREAK_STATE_MACHINE_LOOP;
    };
    private final EventState<E> VALID_CHECKER_STATE = memory -> {
        EventState<E> result;
        CheckPointMatcher<E> cpMatcher = memory.cpMatcher;
        if (cpMatcher.hasSufficientValidContext()) {
            if (cpMatcher.isValid(memory.event)) {
                result = VALID_STATE;
            } else {
                result = INVALID_STATE;
            }
        } else {
            result = ADD_TO_VALID_CONTAINER_STATE;
        }
        return result;
    };
    private final EventState<E> MATCHED_STATE = memory -> {
        memory.cpMatcher.updateContext(memory.event);
        if (isFirstMatchedCheckPoint()) {
            endTimeMillis = memory.event.getTimestampMillis() + maxTimeMillis;
            notifyStart();
            // listenerHandler.fire(of((Event) null, START, FAKE_STATE, //
            // EMPTY_LABEL, context));
        }
        // TODO notifyMatch()
        return VALID_CHECKER_STATE;
    };

    private boolean isFirstMatchedCheckPoint() {
        return endTimeMillis == null;
    }

    // TODO make Enum of above? states

    @Override
    public void _beforeStart(SequenceProcessorHelper<E> helper) {
        this.helper = helper;
        maxTimeMillis = helper.getMaxTimeMillis();
        resetSequence();
        System.out.println("*********** beforeStart: ");
    }

    @Override
    public void _onTimeout() {
        for (Long eventId : checkPointMatchers.keySet()) {
            for (CheckPointMatcher<E> cpMatcher : checkPointMatchers.get(eventId)) {
                notifyCheckPointTimeout(cpMatcher);
                // listenerHandler.fire(of((Event) null, TIMEOUT,
                // cpMatcher.index, EMPTY_LABEL, context));
            }
        }
        notifySequenceTimeout();
        // listenerHandler.fire(of((Event) null, TIMEOUT, FAKE_STATE,
        // EMPTY_LABEL, context));
        notifyEnd();
        // listenerHandler.fire(of((Event) null, END, FAKE_STATE, EMPTY_LABEL,
        // context));
        resetSequence();
        System.out.println("*********** onTimeout: ");
    }

    private void resetSequence() {
        checkPointMatchers = helper.getCheckPointMatchers().stream().collect(groupingBy(CheckPointMatcher::getEventId));
        endTimeMillis = null;
    }

    private static class Memory<E extends Event> {
        final E event;
        final long eventId;
        CheckPointMatcher<E> cpMatcher;

        Memory(E event) {
            this.event = event;
            eventId = event.getId();
        }

        @Override
        public String toString() {
            return "event-id :: " + eventId;
        }
    }

    @SuppressWarnings("serial")
    private static class ExitException extends RuntimeException {
    }

    @FunctionalInterface
    private interface EventState<E extends Event> {
        EventState<E> next(Memory<E> memory);
    }

    @Override
    public void _process(E event) {
        System.out.println("*********** processing: " + event);
        try {
            if (hasSequenceMaxtimePassed(event)) {
                _onTimeout();
            }
            if (!checkPointMatchers.containsKey(event.getId())) {
                return;
            }
            runStateMachine(MATCH_CHECKER_STATE_THAT_THROWS, new Memory<E>(event));
            washContainer(eventsToMatchContainer, MATCH_CHECKER_STATE_THAT_BREAKS_LOOP);
            washContainer(eventsToValidateContainer, VALID_CHECKER_STATE);
            if (isReady()) {
                notifyEnd();
                resetSequence();
            }
        } catch (ExitException e) {
            // exit this method call.
            return;
        }
    }

    private boolean hasSequenceMaxtimePassed(Event event) {
        if (endTimeMillis == null) {
            return false;
        }
        return event.getTimestampMillis() > endTimeMillis;
    }

    private void runStateMachine(EventState<E> state, final Memory<E> m) {
        if (state == BREAK_STATE_MACHINE_LOOP) {
            return;
        }
        runStateMachine(state.next(m), m);
    }

    private void washContainer(List<Memory<E>> container, EventState<E> eventState) {
        for (Memory<E> memory : container) {
            container.remove(memory);
            runStateMachine(eventState, memory);
        }
    }

    private boolean isReady() {
        return checkPointMatchers.isEmpty();
    }

    // private class InitialState implements EventState<E> {
    //
    // @Override
    // public EventState<E> next(Memory<E> m) {
    // if (hasSequenceMaxtimePassed(m.event)) {
    // onTimeout();
    // return EXIT_PROCESSING;
    // }
    // return SEQUENCE_CONTAINS_EVENT_CHECKER_STATE;
    // }
    // }


    // private class SequenceContainsEventCheckerState implements EventState<E>
    // {
    //
    // @Override
    // public EventState<E> next(Memory<E> m) {
    // if (checkPointMatchers.containsKey(m.eventId)) {
    // return MATCH_CHECKER_STATE;
    // } else {
    // return EXIT_PROCESSING;
    // }
    // }
    // }

    private class MatchCheckerState implements EventState<E> {

        private final EventState<E> exitOrBreak;

        MatchCheckerState() {
            this(EXIT_PROCESSING);
        }

        MatchCheckerState(EventState<E> exitOrBreak) {
            this.exitOrBreak = exitOrBreak;
        }

        @Override
        public EventState<E> next(Memory<E> memory) {
            EventState<E> result = null;
            for (CheckPointMatcher<E> cpMatcher : checkPointMatchers.get(memory.eventId)) {
                if (cpMatcher.hasSufficientMatchContext()) {
                    boolean matches = cpMatcher.matches((E) memory.event);
                    if (matches) {
                        memory.cpMatcher = cpMatcher;
                        result = MATCHED_STATE;
                        break;
                    }
                } else {
                    result = ADD_TO_MATCH_CONTAINER_STATE;
                }

            }
            if (result == null) {
                result = exitOrBreak;
            }
            return result;
        }

    }

    private class MatchCheckerStateThatExit implements EventState<E> {
        @Override
        public EventState<E> next(Memory<E> memory) {
            for (CheckPointMatcher<E> cpMatcher : checkPointMatchers.get(memory.eventId)) {
                if (cpMatcher.hasSufficientMatchContext()) {
                    boolean matches = cpMatcher.matches((E) memory.event);
                    if (matches) {
                        memory.cpMatcher = cpMatcher;
                        return MATCHED_STATE;
                    }
                } else {
                    return ADD_TO_MATCH_CONTAINER_STATE;
                }
            }
            return EXIT_PROCESSING;
        }
    }


    private class MatchCheckerStateThatBreaksLoop implements EventState<E> {
        @Override
        public EventState<E> next(Memory<E> memory) {
            for (CheckPointMatcher<E> cpMatcher : checkPointMatchers.get(memory.eventId)) {
                if (cpMatcher.hasSufficientMatchContext()) {
                    boolean matches = cpMatcher.matches((E) memory.event);
                    if (matches) {
                        memory.cpMatcher = cpMatcher;
                        return MATCHED_STATE;
                    }
                } else {
                    return ADD_TO_MATCH_CONTAINER_STATE;
                }
            }
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    private class ValidCheckerState implements EventState<E> {

        @Override
        public EventState<E> next(Memory<E> memory) {
            EventState<E> result;
            CheckPointMatcher<E> cpMatcher = memory.cpMatcher;
            if (cpMatcher.hasSufficientValidContext()) {
                if (cpMatcher.isValid(memory.event)) {
                    result = VALID_STATE;
                } else {
                    result = INVALID_STATE;
                }
            } else {
                result = ADD_TO_VALID_CONTAINER_STATE;
            }
            return result;
        }
    }

    private class AddToValidContainerState implements EventState<E> {
        @Override
        public EventState<E> next(Memory<E> memory) {
            eventsToValidateContainer.add(memory);
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    private class ValidState implements EventState<E> {

        @Override
        public EventState<E> next(Memory<E> memory) {
            removeCheckPoint(memory);
            // String label = memory.cpMatcher.getLabel();
            // int index = memory.cpMatcher.index;
            notifySuccess(memory.event);
            // listenerHandler.fire(of(memory.event, EMIT, index, label,
            // context));
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    private class InvalidState implements EventState<E> {

        @Override
        public EventState<E> next(Memory<E> memory) {
            removeCheckPoint(memory);
            // String label = memory.cpMatcher.getLabel();
            // int index = memory.cpMatcher.index;
            // Value<Event> invalidMatcher =
            // memory.cpMatcher.invalidMatcher(memory.event, context);
            notifyFailure(memory.event);
            // listenerHandler.fire(of(memory.event, INVALID, index, label,
            // context, invalidMatcher));
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    private void removeCheckPoint(Memory<E> memory) {
        checkPointMatchers.get(memory.eventId).remove(memory.cpMatcher);
        if (checkPointMatchers.get(memory.eventId).isEmpty()) {
            checkPointMatchers.remove(memory.eventId);
        }
    }

}
