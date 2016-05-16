/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.tdl.CheckPoint;

class ProcessorLogger implements SequenceProcessorListener {
    private static final String LOG_TEMPLATE = "[CHECKPOINT[%s.%s]:%s]  %s";
    private static final String FAILURE_LOG_TEMPLATE = "[CHECKPOINT[%s.%s]:%s]  %s%s";
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorLogger.class);

    ProcessorLogger() {
        /* for package private usage only */
    }

    @Override
    public void onCheckPointFailure(OnCheckPointFailure on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        String failureString = "\n" + on.getFailureString();
        LOG.info(String.format(FAILURE_LOG_TEMPLATE, seqIndex, cpIndex, "FAILURE", on.getEvent(),
                failureString));
    }

    @Override
    public void onCheckPointMatch(OnCheckPointMatch on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "MATCH", on.getEvent()));
    }

    @Override
    public void onCheckPointSuccess(OnCheckPointSuccess on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "SUCCESS", on.getEvent()));
    }

    @Override
    public void onCheckPointTimeout(OnCheckPointTimeout on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        CheckPoint cp = on.getCheckPoint();
        String template = "%s : match: %s, valid: %s";
        String matchValidDef = String.format(template, cp.getEventType(), cp.getMatch(),
                cp.getValidate());
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "TIMEOUT", matchValidDef));
    }

    @Override
    public void onSequenceStart(OnSequenceStart on) {
        LOG.info("[START] Sequence " + on.getSequenceName() + " started.");
    }

    @Override
    public void onSequenceEnd(OnSequenceEnd on) {
        LOG.info("[END ] Sequence " + on.getSequenceName() + " ended.");
        String delimiter = "\n      ";
        String prefix = "\nContext=" + delimiter;
        String logMessage = on.getContext().entrySet().stream()
                .map(e -> e.toString())
                .collect(Collectors.joining(delimiter, prefix, ""));
        LOG.info(logMessage);
    }

    @Override
    public void onSequenceTimeout(OnSequenceTimeout on) {
        LOG.info("[TIMEOUT] Sequence " + on.getSequenceName() + " timedout.");
    }

}
