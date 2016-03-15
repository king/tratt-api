package com.king.tratt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.spi.Event;
import com.king.tratt.tdl.CheckPoint;

class ProcessorLogger<E extends Event> implements SequenceProcessorListener<E> {
    private static final String LOG_TEMPLATE = "[CHECKPOINT[%s.%s]:%s]  %s";
    private static final String FAILURE_LOG_TEMPLATE = "[CHECKPOINT[%s.%s]:%s]  %s%s";
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorLogger.class);

    public ProcessorLogger() {
    }

    @Override
    public void onCheckPointFailure(OnCheckPointFailure<E> on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        String failureString = "\n" + on.getFailureString();
        LOG.info(String.format(FAILURE_LOG_TEMPLATE, seqIndex, cpIndex, "FAILURE", on.getEvent(), failureString));
    }

    @Override
    public void onCheckPointMatch(OnCheckPointMatch<E> on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "MATCH", on.getEvent()));
    }

    @Override
    public void onCheckPointSuccess(OnCheckPointSuccess<E> on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "SUCCESS", on.getEvent()));
    }

    @Override
    public void onCheckPointTimeout(OnCheckPointTimeout<E> on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        CheckPoint cp = on.getCheckPoint();
        String template = "%s : match: %s, valid: %s";
        String matchValidDef = String.format(template, cp.getEventType(), cp.getMatch(), cp.getValidate());
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "TIMEOUT", matchValidDef));
    }

    @Override
    public void onSequenceStart(OnSequenceStart<E> on) {
        LOG.info("[START] Sequence " + on.getSequenceName() + " started.");
    }

    @Override
    public void onSequenceEnd(OnSequenceEnd<E> on) {
        LOG.info("[END ] Sequence " + on.getSequenceName() + " ended.");
        // TODO log Context
    }

    @Override
    public void onSequenceTimeout(OnSequenceTimeout<E> on) {
        LOG.info("[TIMEOUT] Sequence " + on.getSequenceName() + " timedout.");
    }

}
