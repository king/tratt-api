package com.king.tratt;

import static java.util.Collections.unmodifiableList;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import com.king.tratt.SequenceProcessorListener.OnCheckPointTimeout;
import com.king.tratt.SequenceProcessorListener.OnEnd;
import com.king.tratt.SequenceProcessorListener.OnFailure;
import com.king.tratt.SequenceProcessorListener.OnMatch;
import com.king.tratt.SequenceProcessorListener.OnSequenceTimeout;
import com.king.tratt.SequenceProcessorListener.OnStart;
import com.king.tratt.SequenceProcessorListener.OnSuccess;
import com.king.tratt.tdl.Sequence;

abstract class SequenceProcessor<E extends Event> {

    private List<SequenceProcessorListener<E>> sequenceListeners;
    private Sequence sequence;
    private Environment<E> env;
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

    final void setEnv(Environment<E> env) {
        this.env = env;
    }

    void setContext(Context context) {
        this.context = context;
    }

    public final void notifyStart() {
        notify(listener -> listener.onStart(new OnStart<E>(getName())));
    }

    public final void notifyEnd() {
        notify(listener -> listener.onEnd(new OnEnd<E>(getName())));
    }

    public final void notifyMatch() {
        notify(listener -> listener.onMatch(new OnMatch<E>(getName())));
    }

    public final void notifyFailure(E event) {
        notify(listener -> listener.onFailure(new OnFailure<E>(getName())));
    }

    public final void notifySuccess(E event) {
        notify(listener -> listener.onSuccess(new OnSuccess<E>(getName())));
    }

    public final void notifyCheckPointTimeout(CheckPointMatcher<E> cpMatcher) {
        notify(listener -> listener.onCheckPointTimeout(new OnCheckPointTimeout<E>(getName())));
    }

    public final void notifySequenceTimeout() {
        notify(listener -> listener.onSequenceTimeout(new OnSequenceTimeout<E>(getName())));
    }

    private void notify(Consumer<SequenceProcessorListener<E>> c) {
        sequenceListeners.forEach(c);
    }
}
