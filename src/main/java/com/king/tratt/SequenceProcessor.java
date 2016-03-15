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
        _beforeStart(new SequenceProcessorHelper<E>(checkPointMatchers, sequence, sequenceListeners));
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

    final String getName() {
        return sequence.getName();
    }

    final static class SequenceProcessorHelper<E extends Event> {
        private final List<CheckPointMatcher<E>> checkPointMatchers;
        private final Sequence sequence;
        private final List<SequenceProcessorListener<E>> sequenceListeners;
        private final String seqName;

        SequenceProcessorHelper(List<CheckPointMatcher<E>> checkPointMatchers,
                Sequence sequence, List<SequenceProcessorListener<E>> sequenceListeners) {
            this.checkPointMatchers = checkPointMatchers;
            this.sequence = sequence;
            this.sequenceListeners = sequenceListeners;
            this.seqName = sequence.getName();
        }

        List<CheckPointMatcher<E>> getCheckPointMatchers() {
            return checkPointMatchers;
        }

        long getMaxTimeMillis() {
            Duration d = Duration.parse(sequence.getSequenceMaxTime());
            return d.getSeconds() * 1000;
        }

        final void notifySequenceStart(Context context) {
            notify(listener -> listener.onSequenceStart(new OnSequenceStart<E>(seqName, context)));
        }

        final void notifySequenceEnd(Context context) {
            notify(listener -> listener.onSequenceEnd(
                    new OnSequenceEnd<E>(seqName, context)));
        }

        final void notifySequenceTimeout(Context context) {
            notify(listener -> listener.onSequenceTimeout(
                    new OnSequenceTimeout<E>(seqName, context)));
        }

        final void notifyCheckPointMatch(E event, CheckPointMatcher<E> cpMatcher, Context context) {
            notify(listener -> listener.onCheckPointMatch(
                    new OnCheckPointMatch<E>(seqName, event, cpMatcher, context)));
        }

        final void notifyCheckPointFailure(E event, CheckPointMatcher<E> cpMatcher, Context context) {
            notify(listener -> listener.onCheckPointFailure(
                    new OnCheckPointFailure<E>(seqName, event, cpMatcher, context)));
        }

        final void notifyCheckPointSuccess(E event, CheckPointMatcher<E> cpMatcher, Context context) {
            notify(listener -> listener.onCheckPointSuccess(
                    new OnCheckPointSuccess<E>(seqName, event, cpMatcher, context)));
        }

        final void notifyCheckPointTimeout(CheckPointMatcher<E> cpMatcher, Context context) {
            notify(listener -> listener.onCheckPointTimeout(
                    new OnCheckPointTimeout<E>(seqName, cpMatcher, context)));
        }

        private void notify(Consumer<SequenceProcessorListener<E>> c) {
            sequenceListeners.forEach(c);
        }

        Context newContext() {
            return new ContextImp();
        }
    }
}
