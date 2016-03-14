package com.king.tratt._temp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.SequenceProcessorListener;
import com.king.tratt.metadata.spi.Event;

public class SequenceLogger<E extends Event> implements SequenceProcessorListener<E> {

    private static final String LOG_TEMPLATE = "[CHECKPOINT[%s.%s]:%s]  %s";
    private static final Logger LOG = LoggerFactory.getLogger(SequenceLogger.class);
    // private final Tdl tdl;
    // private final Context context = EMPTY_CONTEXT;

    // private static final Context EMPTY_CONTEXT = new Context() {
    //
    // Map<Object, Object> ctx = new HashMap<>();
    //
    // @Override
    // public Object getValue(Object key) {
    // return ctx.get(key);
    // }
    //
    // @Override
    // public void setValue(Object key, Object value) {
    // ctx.put(key, value);
    // }
    //
    // @Override
    // public Map<Object, Object> getAllValues() {
    // return ctx;
    // }
    // };

    public SequenceLogger() {
    }

    @Override
    public void onCheckPointFailure(OnCheckPointFailure<E> on) {
        int seqIndex = on.getSequenceIndex();
        int cpIndex = on.getCheckPointIndex();
        String failure = on.getFailureString();
        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "FAILURE", on.getEvent()));
        // TODO log failure
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

        LOG.info(String.format(LOG_TEMPLATE, seqIndex, cpIndex, "TIMEOUT", on.getEvent()));
        // occuredEvent = checkPoint.getEventType() + " : " + "match: " +
        // checkPoint.getMatch() +
        // ", valid: " + checkPoint.getValidate();
        // }

    }

    @Override
    public void onSequenceStart(OnSequenceStart<E> on) {
        LOG.info("[START] Sequence " + on.getSequenceName() + " started.");
        // TODO log Context
    }

    @Override
    public void onSequenceEnd(OnSequenceEnd<E> on) {
        LOG.info("[END ] Sequence " + on.getSequenceName() + " ended.");
    }

    @Override
    public void onSequenceTimeout(OnSequenceTimeout<E> on) {
        LOG.info("[TIMEOUT] Sequence " + on.getSequenceName() + " timedout.");
    }

    // @Override
    // public void emit(ProcessEvent<Event> e) {
    // if (e.getContext() != null) {
    // context.getAllValues().putAll(e.getContext().getAllValues());
    // }
    // final String processorName = e.getProcessorId() == null ? "Unknown" :
    // e.getProcessorId().toString();
    //
    // switch (e.getType()) {
    // case INVALID:
    // LOG.warn(formatLogMessage(e));
    // break;
    // case TIMEOUT:
    // if (isValidState(e)) {
    // LOG.warn(formatCheckPointLogMessage(e,
    // "\n There where no events matching this CheckPoint"));
    // } else {
    // LOG.warn("[TIMEOUT] Sequence " + processorName + " timedout.");
    // }
    // break;
    // case INVALID_STATE:
    // LOG.warn(formatCheckPointLogMessage(e,
    // "\n Events not matched in correct order. Example: Event3 occurred before
    // Event2"));
    // break;
    // case START:
    // LOG.info("[START] Sequence " + processorName + " started.");
    // break;
    // case END:
    // LOG.info("[END ] Sequence " + processorName + " ended.");
    // logContext();
    // break;
    // case EMIT:
    // LOG.info(formatLogMessage(e));
    // break;
    // }
    // }
    //
    // boolean isValidState(ProcessEvent<Event> e) {
    // String seqName = (String) e.getProcessorId();
    // if (tdl.containsSequence(seqName)) {
    // Sequence sequence = tdl.getSequence(seqName);
    // return e.getState() >= 0 && e.getState() <
    // sequence.getCheckPoints().size();
    // }
    // return false;
    // }
    //
    // private void logContext() {
    // StringBuilder logMessage = new StringBuilder().append("\n");
    // formatContextLogMessage(logMessage, "context", context.getAllValues());
    // LOG.info(logMessage.toString());
    // }
    //
    // private void formatContextLogMessage(StringBuilder sb, String name,
    // Map<?, ?> map) {
    // sb.append(name).append("=\n");
    // String indent = " ";
    // for (Object key : map.keySet()) {
    // sb.append(indent)
    // .append(key).append("=").append(map.get(key))
    // .append("\n");
    // }
    // }
    //
    // private String formatLogMessage(ProcessEvent<Event> e) {
    // return formatCheckPointLogMessage(e, "");
    // }
    //
    // private String formatCheckPointLogMessage(ProcessEvent<Event> e, String
    // extraMessage) {
    // Sequence sequence = tdl.getSequence((String) e.getProcessorId());
    //
    // CheckPoint checkPoint = null;
    // Event event = e.getEvent();
    // if (isValidState(e)) {
    // checkPoint = sequence.getCheckPoints().get(e.getState());
    // }
    //
    // String occuredEvent = "";
    // if (event != null) {
    // occuredEvent = OccuredEvent.of(event,
    // metaDataProvider.getMetaData(event)).toString();
    // } else if (checkPoint != null) {
    // occuredEvent = checkPoint.getEventType() + " : " + "match: " +
    // checkPoint.getMatch() +
    // ", valid: " + checkPoint.getValidate();
    // }
    //
    // String INDENT = " ";
    // String matcherResult;
    // if (e.getInvalidMatcher() == null) {
    // matcherResult = "";
    // } else {
    // String message = e.getInvalidMatcher().toDebugString(e.getEvent(),
    // e.getContext());
    // // remove "source:", white spaces and brackets, then split on "&&"
    // message = message
    // .replaceAll("source:", "")
    // .replaceAll("\\s*\\(\\s*", "")
    // .replaceAll("\\s*\\)\\s*", "")
    // .replaceAll("\\s*&&\\s*", " &&\n" + INDENT);
    // matcherResult = "\n" + INDENT + message;
    // }
    //
    // return format(LOG_TEMPLATE, e.getState(), e.getType(), occuredEvent,
    // matcherResult, extraMessage);
    // }

}
