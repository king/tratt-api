// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

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

abstract class SequenceProcessor {

    private List<SequenceProcessorListener> sequenceListeners;
    private Sequence sequence;
    private List<CheckPointMatcher> checkPointMatchers;

    protected abstract void beforeStartImp(SequenceProcessorHelper helper);

    protected abstract void onTimeoutImp();

    protected abstract void processImp(Event e);

    final void onTimeout() {
        onTimeoutImp();
    }

    final void process(Event e) {
        processImp(e);
    }

    final void beforeStart() {
        beforeStartImp(new SequenceProcessorHelper(checkPointMatchers, sequence, sequenceListeners));
    }

    final void setCheckPointMatchers(List<CheckPointMatcher> cpMatchers) {
        this.checkPointMatchers = unmodifiableList(cpMatchers);
    }

    final void setListeners(List<SequenceProcessorListener> sequenceListeners) {
        this.sequenceListeners = sequenceListeners;
    }

    final void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    final String getName() {
        return sequence.getName();
    }

    final class SequenceProcessorHelper {
        private final List<CheckPointMatcher> checkPointMatchers;
        private final Sequence sequence;
        private final List<SequenceProcessorListener> sequenceListeners;
        private final String seqName;

        SequenceProcessorHelper(List<CheckPointMatcher> checkPointMatchers,
                Sequence sequence, List<SequenceProcessorListener> sequenceListeners) {
            this.checkPointMatchers = checkPointMatchers;
            this.sequence = sequence;
            this.sequenceListeners = sequenceListeners;
            this.seqName = sequence.getName();
        }

        List<CheckPointMatcher> getCheckPointMatchers() {
            return checkPointMatchers;
        }

        long getMaxTimeMillis() {
            Duration d = Duration.parse(sequence.getSequenceMaxTime());
            return d.getSeconds() * 1000;
        }

        final void notifySequenceStart(Context context) {
            notify(listener -> listener.onSequenceStart(new OnSequenceStart(seqName, context)));
        }

        final void notifySequenceEnd(Context context) {
            notify(listener -> listener.onSequenceEnd(
                    new OnSequenceEnd(seqName, context)));
        }

        final void notifySequenceTimeout(Context context) {
            notify(listener -> listener.onSequenceTimeout(
                    new OnSequenceTimeout(seqName, context)));
        }

        final void notifyCheckPointMatch(Event event, CheckPointMatcher cpMatcher,
                Context context) {
            notify(listener -> listener.onCheckPointMatch(
                    new OnCheckPointMatch(seqName, event, cpMatcher, context)));
        }

        final void notifyCheckPointFailure(Event event, CheckPointMatcher cpMatcher,
                Context context) {
            notify(listener -> listener.onCheckPointFailure(
                    new OnCheckPointFailure(seqName, event, cpMatcher, context)));
        }

        final void notifyCheckPointSuccess(Event event, CheckPointMatcher cpMatcher,
                Context context) {
            notify(listener -> listener.onCheckPointSuccess(
                    new OnCheckPointSuccess(seqName, event, cpMatcher, context)));
        }

        final void notifyCheckPointTimeout(CheckPointMatcher cpMatcher, Context context) {
            notify(listener -> listener.onCheckPointTimeout(
                    new OnCheckPointTimeout(seqName, cpMatcher, context)));
        }

        private void notify(Consumer<SequenceProcessorListener> c) {
            sequenceListeners.forEach(c);
        }

        Context newContext() {
            return new ContextImp();
        }
    }
}
