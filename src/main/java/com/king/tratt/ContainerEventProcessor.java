package com.king.tratt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.king.tratt.spi.Event;

class ContainerEventProcessor<E extends Event> extends SequenceProcessor<E> {

    static final int FAKE_STATE = -1;
    static final String EMPTY_LABEL = "";
    ProcessListenerHandler<Event> listenerHandler;
    private Map<Long, List<DecoratedCheckPoint>> checkPoints;
    private List<Memory> matchContainer = new CopyOnWriteArrayList<>();
    private List<Memory> validContainer = new CopyOnWriteArrayList<>();
    private Long endTimeMillis;
    Context context;
    private long maxTimeMillis;
    private boolean isStopped = false;
    private final Object trattId;

    ContainerEventProcessor(String trattId, long maxTimeMillis,
            Map<Long, List<DecoratedCheckPoint>> checkPoints, Context context, Environment environment) {
        this.maxTimeMillis = maxTimeMillis;
        this.checkPoints = checkPoints;
        this.context = context;
        this.trattId = trattId;
        listenerHandler = new ProcessListenerHandler<>(trattId);

    }

    private class Memory {

        final E event;
        final long eventId;
        DecoratedCheckPoint decoratedCheckPoint;

        Memory(Event event) {
            this.event = event;
            eventId = event.getEventType();
        }

        @Override
        public String toString() {
            return EventType.getById(event.getEventType()) + " :: " + event;
        }
    }

    @SuppressWarnings("serial")
    private static class ExitException extends RuntimeException {

    }

    interface EventState {

        public static final EventState EXIT_PROCESSING = new EventState() {

            @Override
            public EventState next(Memory memory) {
                throw new ExitException();
            };
        };
        public static final EventState BREAK_STATE_MACHINE_LOOP = new EventState() {
            @Override
            public EventState next(Memory memory) {
                throw new UnsupportedOperationException();
            }
        };

        EventState next(Memory memory);
    }

    @Override
    public void process(Event event) throws Exception {
        try {
            runStateMachine(new InitialState(), new Memory(event));
            washContainer(matchContainer, new MatchCheckerState(BREAK_STATE_MACHINE_LOOP));
            washContainer(validContainer, new ValidCheckerState());

            if (isReady()) {
                isStopped = true;
                listenerHandler.fire(of((Event) null, END, FAKE_STATE, EMPTY_LABEL, context));
            }

        } catch (ExitException e) {
            // exit this method call.
            return;
        }
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

    private class InitialState implements EventState {

        @Override
        public EventState next(Memory m) {
            if (isStopped) {
                return EXIT_PROCESSING;
            }
            if (hasSequenceMaxtimePassed(m.event)) {
                onTimeout();
                return EXIT_PROCESSING;
            }
            return new RelevantEventCheckerState();
        }
    }

    private boolean hasSequenceMaxtimePassed(Event event) {
        if (endTimeMillis == null) {
            return false;
        }
        return event.getTimeStamp() > endTimeMillis;
    }

    private boolean isReady() {
        return checkPoints.isEmpty();
    }

    private class RelevantEventCheckerState implements EventState {

        @Override
        public EventState next(Memory m) {
            if (checkPoints.containsKey(m.eventId)) {
                return new MatchCheckerState();
            } else {
                return EXIT_PROCESSING;
            }
        }
    }

    private class MatchCheckerState implements EventState {

        private final EventState exitOrBreak;

        public MatchCheckerState() {
            this(EXIT_PROCESSING);
        }

        public MatchCheckerState(EventState exitOrBreak) {
            this.exitOrBreak = exitOrBreak;
        }

        @Override
        public EventState next(Memory memory) {
            EventState result = null;
            for (DecoratedCheckPoint decoratedCheckPoint : checkPoints.get(memory.eventId)) {
                if (decoratedCheckPoint.hasSufficientMatchContext(context)) {
                    if (decoratedCheckPoint.matches(memory.event, context)) {
                        memory.decoratedCheckPoint = decoratedCheckPoint;
                        result = new MatchedState();
                        break;
                    }
                } else {
                    result = new AddToMatchContainerState();
                }

            }
            if (result == null) {
                return exitOrBreak;
            }
            return result;
        }

    }

    class AddToMatchContainerState implements EventState {

        @Override
        public EventState next(Memory memory) {
            matchContainer.add(memory);
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    class MatchedState implements EventState {

        @Override
        public EventState next(Memory memory) {
            memory.decoratedCheckPoint.updateContext(memory.event, context);
            if (isFirstMatchedCheckPoint()) {
                endTimeMillis = memory.event.getTimeStamp() + maxTimeMillis;
                listenerHandler.fire(of((Event) null, START, FAKE_STATE, EMPTY_LABEL, context));
            }
            return new ValidCheckerState();
        }
    }

    private boolean isFirstMatchedCheckPoint() {
        return endTimeMillis == null;
    }

    class ValidCheckerState implements EventState {

        @Override
        public EventState next(Memory memory) {
            EventState result;
            DecoratedCheckPoint cp = memory.decoratedCheckPoint;
            if (cp.hasSufficientValidContext(context)) {
                if (cp.isValid(memory.event, context)) {
                    result = new ValidState();
                } else {
                    result = new InvalidState();
                }

            } else {
                result = new AddToValidContainerState();
            }
            return result;
        }
    }

    class AddToValidContainerState implements EventState {

        @Override
        public EventState next(Memory memory) {
            validContainer.add(memory);
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    class ValidState implements EventState {

        @Override
        public EventState next(Memory memory) {
            removeCheckPoint(memory);
            String label = memory.decoratedCheckPoint.getLabel();
            int index = memory.decoratedCheckPoint.index;
            listenerHandler.fire(of(memory.event, EMIT, index, label, context));
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    class InvalidState implements EventState {

        @Override
        public EventState next(Memory memory) {
            removeCheckPoint(memory);
            String label = memory.decoratedCheckPoint.getLabel();
            int index = memory.decoratedCheckPoint.index;
            Value<Event> invalidMatcher = memory.decoratedCheckPoint.invalidMatcher(memory.event, context);
            listenerHandler.fire(of(memory.event, INVALID, index, label, context, invalidMatcher));
            return BREAK_STATE_MACHINE_LOOP;
        }
    }

    private void removeCheckPoint(Memory memory) {
        checkPoints.get(memory.eventId).remove(memory.decoratedCheckPoint);
        if (checkPoints.get(memory.eventId).isEmpty()) {
            checkPoints.remove(memory.eventId);
        }
    }

    @Override
    public void addListener(ProcessListener<Event> processListener) {
        listenerHandler.add(processListener);
    }

    @Override
    public Object getProcessorId() {
        return trattId;
    }

    @Override
    public void onTimeout() {
        if (!isStopped) {
            isStopped = true;
            for (Long eventId : checkPoints.keySet()) {
                for (DecoratedCheckPoint cp : checkPoints.get(eventId)) {
                    listenerHandler.fire(of((Event) null, TIMEOUT, cp.index, EMPTY_LABEL, context));
                }
            }
            listenerHandler.fire(of((Event) null, TIMEOUT, FAKE_STATE, EMPTY_LABEL, context));
            listenerHandler.fire(of((Event) null, END, FAKE_STATE, EMPTY_LABEL, context));
        }
    }

}
