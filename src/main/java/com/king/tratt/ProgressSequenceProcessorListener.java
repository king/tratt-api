package com.king.tratt;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.king.tratt.tdl.Sequence;

class ProgressSequenceProcessorListener<E extends Event> implements CompletionStrategy<E> {
    private final Map<String, SequenceStatus> processorMap;
    private final AtomicInteger open;

    public ProgressSequenceProcessorListener(List<Sequence> sequences) {
        processorMap = sequences.stream().map(Sequence::getName).collect(toMap(identity(), SequenceStatus::new));
        open = new AtomicInteger(sequences.size());
    }

    @Override
    public boolean isCompleted() {
        return open.get() <= 0;
    }

    @Override
    public void onStart(OnStart<E> onStart) {
        status(onStart).started = true;
    }

    @Override
    public void onEnd(OnEnd<E> onEnd) {
        status(onEnd).done = true;
        open.getAndDecrement();
    }

    @Override
    public void onFailure(OnFailure<E> onFailure) {
        status(onFailure).invalid = true;
    }

    @Override
    public void onTimeout(OnTimeout<E> onTimeout) {
        status(onTimeout).timeout = true;
    }

    
    SequenceStatus status(Base<?> base) {
        return processorMap.get(base.getSequenceName());
    }

    static class SequenceStatus {

        private final String seqName;
        private boolean started = false;
        private boolean done = false;
        private boolean invalid = false;
        private boolean timeout = false;

        public SequenceStatus(String seqName) {
            this.seqName = seqName;
        }

        public String getName() {
            return seqName;
        }

        public boolean isDone() {
            return done;
        }

        public boolean isInvalid() {
            return invalid;
        }

        public boolean hasTimeout() {
            return timeout;
        }

        public boolean isStarted() {
            return started;
        }
    }

    public Collection<SequenceStatus> getStatus() {
        return processorMap.values();
    }

}
