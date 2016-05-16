/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.SequenceResult.Cause.INVALID_FIELDS;
import static com.king.tratt.SequenceResult.Cause.NOT_CLOSED;
import static com.king.tratt.SequenceResult.Cause.NOT_STARTED;
import static com.king.tratt.SequenceResult.Cause.TIMEOUT;
import static com.king.tratt.SequenceResult.Cause.VALID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import com.king.tratt.ProgressSequenceProcessorListener.SequenceStatus;
import com.king.tratt.SequenceResult.Cause;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;
import com.king.tratt.tdl.CheckPoint;
import com.king.tratt.tdl.Sequence;
import com.king.tratt.tdl.Tdl;

class TdlProcessor {
    private static final String VARIABLE_PREFIX = "$";

    private final CachingProcessor cachedEvents;
    private final StartedEventProcessor started;
    private final CompletionStrategy completionStrategy;

    TdlProcessor(CachingProcessor cachedEvents, StartedEventProcessor started) {
        this.cachedEvents = cachedEvents;
        this.started = started;
        this.completionStrategy = started.completionStrategy;
    }

    List<SequenceResult> processTdl(Tdl tdl) {
        MatcherParser matcherParser = new MatcherParser(started.valueFactory);
        Map<String, String> tdlVariables = VariableParser.parse(VARIABLE_PREFIX, tdl.getVariables());
        CopyOnWriteArrayList<SequenceProcessor> processors = createSequenceProcessors(tdl, matcherParser, tdlVariables);
        processors.parallelStream().forEach(processor -> processor.beforeStart());

        try {
            return startProcessing(processors);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }

    private CopyOnWriteArrayList<SequenceProcessor> createSequenceProcessors(Tdl tdl, MatcherParser matcherParser,
            Map<String, String> tdlVariables) {
        /*
         * We need to store the sequence index, hence the use of IntStream to
         * loop through Sequences.
         */
        int numOfSequences = tdl.getSequences().size();
        try (IntStream indexStream = IntStream.range(0, numOfSequences)) {
            return indexStream
                    .mapToObj(seqIndex -> {
                        Sequence sequence = tdl.getSequences().get(seqIndex);
                        List<CheckPoint> checkPoints = sequence.getCheckPoints();
                        SetterToValueMapper mapper = new SetterToValueMapper(started.valueFactory);
                        Environment env = createEnv(tdlVariables, checkPoints, mapper);
                        List<CheckPointMatcher> cpMatchers = createCheckPointMatchers(matcherParser, seqIndex,
                                checkPoints, env, mapper);
                        return createSequenceProcessor(sequence, cpMatchers);
                    })
                    .collect(collectingAndThen(toList(), CopyOnWriteArrayList<SequenceProcessor>::new));
        }
    }

    private Environment createEnv(Map<String, String> tdlVariables, List<CheckPoint> checkPoints,
            SetterToValueMapper mapper) {
        Environment env = new Environment(tdlVariables);
        Map<String, Value> map = checkPoints.stream()
                .flatMap(mapper::getValues)
                .collect(toMap(Entry::getKey, Entry::getValue));
        env.sequenceVariables.putAll(map);
        return env;
    }

    private List<CheckPointMatcher> createCheckPointMatchers(MatcherParser matcherParser, int seqIndex,
            List<CheckPoint> checkPoints,
            Environment env, SetterToValueMapper mapper) {
        try (IntStream intStream = IntStream.range(0, checkPoints.size())) {
            return intStream
                    .mapToObj(cpIndex -> {
                        CheckPoint cp = checkPoints.get(cpIndex);
                        Map<String, Value> valuesToStore;
                        valuesToStore = mapper.getValues(cp).collect(toMap(Entry::getKey, Entry::getValue));
                        return new CheckPointMatcher(seqIndex, cpIndex, cp, env, matcherParser,
                                started, valuesToStore);
                    })
                    .collect(toList());
        }
    }

    private SequenceProcessor createSequenceProcessor(Sequence sequence, List<CheckPointMatcher> cpMatchers) {
        SequenceProcessor processor = new ContainerSequenceProcessor();
        processor.setCheckPointMatchers(cpMatchers);
        processor.setListeners(started.sequenceListeners);
        processor.setSequence(sequence);
        return processor;
    }

    private List<SequenceResult> startProcessing(CopyOnWriteArrayList<SequenceProcessor> processors)
            throws InterruptedException {
        TimeoutChecker timeout = new TimeoutChecker(started);
        completionStrategy.beforeStart(new Processors(processors));
        while (!completionStrategy.isCompleted()) {
            if (timeout.hasOccured()) {
                processors.parallelStream().forEach(p -> p.onTimeout());
                break;
            }
            Event event = cachedEvents.poll(500, MILLISECONDS);
            if (event != null) {
                processors.parallelStream().forEach(p -> p.process(event));
            }
        }
        return createSequenceResults(started.progressListener);
    }

    private List<SequenceResult> createSequenceResults(
            ProgressSequenceProcessorListener progressListener) {
        final List<SequenceResult> result = new ArrayList<>();
        for (SequenceStatus s : progressListener.getStatus()) {
            String name = s.getName();
            boolean isValid = !(s.hasTimeout() || s.isInvalid());
            List<Cause> causes = getCause(s);
            result.add(new SequenceResult(name, isValid, causes));
        }
        return result;
    }

    private List<Cause> getCause(SequenceStatus s) {
        if (s.isDone() && !s.isInvalid() && !s.hasTimeout()) {
            return Collections.singletonList(VALID);
        }
        final List<Cause> result = new ArrayList<>();
        if (!s.isStarted()) {
            result.add(NOT_STARTED);
        }
        if (!s.isDone()) {
            result.add(NOT_CLOSED);
        }
        if (s.hasTimeout()) {
            result.add(TIMEOUT);
        }
        if (s.isInvalid()) {
            result.add(INVALID_FIELDS);
        }
        return result;
    }
}
