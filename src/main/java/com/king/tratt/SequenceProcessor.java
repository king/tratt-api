package com.king.tratt;

import static java.util.Collections.unmodifiableList;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import com.king.tratt.SequenceProcessorListener.OnCheckPointFailure;
import com.king.tratt.SequenceProcessorListener.OnCheckPointMatch;
import com.king.tratt.SequenceProcessorListener.OnCheckPointSuccess;
import com.king.tratt.SequenceProcessorListener.OnCheckPointTimeout;
import com.king.tratt.SequenceProcessorListener.OnSequenceEnd;
import com.king.tratt.SequenceProcessorListener.OnSequenceStart;
import com.king.tratt.SequenceProcessorListener.OnSequenceTimeout;
import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.tdl.Sequence;

abstract class SequenceProcessor<E extends Event> {

    private List<SequenceProcessorListener<E>> sequenceListeners;
    private Sequence sequence;
    private List<CheckPointMatcher<E>> checkPointMatchers;
    private Context context;

    protected abstract void _beforeStart(SequenceProcessorHelper<E> helper);

    protected abstract void _onTimeout();

    protected abstract void _process(E e);

    final void onTimeout() {
        _onTimeout();
    }

    final void process(E e) {
        _process(e);
    }

    final void beforeStart() {
        _beforeStart(new SequenceProcessorHelper<E>(checkPointMatchers, sequence));
    }

    public final static class SequenceProcessorHelper<E extends Event> {

        private final List<CheckPointMatcher<E>> checkPointMatchers;
        private final Sequence sequence;

        public SequenceProcessorHelper(List<CheckPointMatcher<E>> checkPointMatchers,
                Sequence sequence) {
            this.checkPointMatchers = checkPointMatchers;
            this.sequence = sequence;
        }

        public List<CheckPointMatcher<E>> getCheckPointMatchers() {
            return checkPointMatchers;
        }

        public long getMaxTimeMillis() {
            Duration d = Duration.parse(sequence.getSequenceMaxTime());
            return d.getSeconds() * 1000;
        }

    }

    final String getName() {
        return sequence.getName();
    }

    final void setCheckPointMatchers(List<CheckPointMatcher<E>> cpMatchers) {
        this.checkPointMatchers = unmodifiableList(cpMatchers);
    }

    final void setListeners(List<SequenceProcessorListener<E>> sequenceListeners) {
        this.sequenceListeners = sequenceListeners;
    }

    final void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    void setContext(Context context) {
        this.context = context;
    }

    public final void notifySequenceStart() {
        notify(listener -> listener.onSequenceStart(new OnSequenceStart<E>(getName())));
    }

    public final void notifySequenceEnd() {
        notify(listener -> listener.onSequenceEnd(new OnSequenceEnd<E>(getName())));
    }

    public final void notifySequenceTimeout() {
        notify(listener -> listener.onSequenceTimeout(new OnSequenceTimeout<E>(getName())));
    }

    public final void notifyCheckPointMatch(E event, CheckPointMatcher<E> cpMatcher) {
        notify(listener -> listener.onCheckPointMatch(new OnCheckPointMatch<E>(getName(), event, cpMatcher)));
    }

    public final void notifyCheckPointFailure(E event, CheckPointMatcher<E> cpMatcher) {
        notify(listener -> listener.onCheckPointFailure(new OnCheckPointFailure<E>(getName(), event, cpMatcher)));
    }

    public final void notifyCheckPointSuccess(E event, CheckPointMatcher<E> cpMatcher) {
        notify(listener -> listener.onCheckPointSuccess(new OnCheckPointSuccess<E>(getName(), event, cpMatcher)));
    }

    public final void notifyCheckPointTimeout(CheckPointMatcher<E> cpMatcher) {
        notify(listener -> listener.onCheckPointTimeout(new OnCheckPointTimeout<E>(getName(), cpMatcher)));
    }

    private void notify(Consumer<SequenceProcessorListener<E>> c) {
        sequenceListeners.forEach(c);
    }
}
