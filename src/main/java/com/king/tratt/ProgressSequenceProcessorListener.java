package com.king.tratt;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.king.tratt.tdl.Sequence;

class ProgressSequenceProcessorListener implements CompletionStrategy {
    private final Map<String, SequenceStatus> processorMap;
    private final AtomicInteger open;
    private Processors processors;

    ProgressSequenceProcessorListener(List<Sequence> sequences) {
        processorMap = sequences.stream().map(Sequence::getName).collect(toMap(identity(), SequenceStatus::new));
        open = new AtomicInteger(sequences.size());
    }

    @Override
    public void beforeStart(Processors processors) {
        this.processors = processors;
    }

    @Override
    public boolean isCompleted() {
        return open.get() <= 0;
    }

    @Override
    public void onSequenceStart(OnSequenceStart on) {
        status(on).started = true;
    }

    @Override
    public void onSequenceEnd(OnSequenceEnd on) {
        status(on).done = true;
        processors.removeProcessor(on.getSequenceName());
        open.getAndDecrement();
    }

    @Override
    public void onSequenceTimeout(OnSequenceTimeout on) {
        status(on).timeout = true;
    }

    @Override
    public void onCheckPointFailure(OnCheckPointFailure on) {
        status(on).invalid = true;
    }

    @Override
    public void onCheckPointTimeout(OnCheckPointTimeout on) {
        status(on).timeout = true;
    }

    
    SequenceStatus status(OnBase base) {
        return processorMap.get(base.getSequenceName());
    }

    public Collection<SequenceStatus> getStatus() {
        return processorMap.values();
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
}
