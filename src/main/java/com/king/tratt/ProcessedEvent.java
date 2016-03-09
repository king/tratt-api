package com.king.tratt.spi;

import com.king.tratt.spi.ProcessListener.EmitType;

public class ProcessedEvent<E extends Event> {

    private EmitType type;
    private int state;
    private final String stateLabel;
    private Context context;
    private Value<E> invalidMatcher;
    private Object processorId;
    private E event;
    
    public static <E  extends Event> ProcessedEvent<E> of(E event, EmitType type, int state, String stateLabel, Context context) {
        return of(event, type, state, stateLabel, context, null);
    }

    public static <E  extends Event> ProcessedEvent<E> of(E event, EmitType type, int state, String stateLabel, Context context, Value<E> invalidMatcher) {
        return new ProcessedEvent<E>(event, type, state, stateLabel, context, invalidMatcher);
    }

    private ProcessedEvent(E event, EmitType type, int state, String stateLabel, Context context, Value<E> invalidMatcher) {
        this.event = event;
        this.type = type;
        this.state = state;
        this.stateLabel = stateLabel;
        this.context = context;
        this.invalidMatcher = invalidMatcher;
    }

    ProcessedEvent<E> setProcessorId(Object processId) {
        this.processorId = processId;
        return this;
    }

    public E getEvent() {
        return event;
    }

    public EmitType getEmitType() {
        return type;
    }

    public Context getContext() {
        return context;
    }

    public Object getProcessorId() {
        return processorId;
    }

    public Value<E> getInvalidMatcher() {
        return invalidMatcher;
    }

    public int getState() {
        return state;
    }

    public String getStateLabel() {
    	return stateLabel;
    }
}
