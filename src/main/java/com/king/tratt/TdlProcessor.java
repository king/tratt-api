package com.king.tratt;

import static com.king.tratt.SequenceResult.Cause.INVALID_FIELDS;
import static com.king.tratt.SequenceResult.Cause.NOT_CLOSED;
import static com.king.tratt.SequenceResult.Cause.NOT_STARTED;
import static com.king.tratt.SequenceResult.Cause.TIMEOUT;
import static com.king.tratt.SequenceResult.Cause.VALID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.king.tratt.ProgressSequenceProcessorListener.SequenceStatus;
import com.king.tratt.SequenceResult.Cause;
import com.king.tratt.metadata.spi.Event;
import com.king.tratt.metadata.spi.Value;
import com.king.tratt.tdl.CheckPoint;
import com.king.tratt.tdl.Sequence;
import com.king.tratt.tdl.Tdl;

class TdlProcessor<E extends Event> {
    private static final String VARIABLE_PREFIX = "$";

    private final CachedProcessor<E> cachedEvents;
    private final StartedEventProcessor<E> started;
    private final CompletionStrategy<E> completionStrategy;

    public TdlProcessor(CachedProcessor<E> cachedEvents, StartedEventProcessor<E> started) {
        this.cachedEvents = cachedEvents;
        this.started = started;
        this.completionStrategy = started.completionStrategy;
    }

    public List<SequenceResult> processTdl(Tdl tdl) {
        MatcherParser<E> matcherParser = new MatcherParser<>(started.valueFactory);
        Map<String, String> tdlVariables = VariableParser.parse(VARIABLE_PREFIX, tdl.getVariables());
        CopyOnWriteArrayList<SequenceProcessor<E>> processors;
        processors = IntStream.range(0, tdl.getSequences().size()).mapToObj(seqIndex -> {
            Sequence sequence = tdl.getSequences().get(seqIndex);
            ContextImp context = new ContextImp();
            List<CheckPoint> checkPoints = sequence.getCheckPoints();
            Environment<E> env = new Environment<E>(tdlVariables);
            SetterToValueMapper<E> mapper = new SetterToValueMapper<>(started.valueFactory);
            env.sequenceVariables.putAll(
                    checkPoints.stream().flatMap(mapper::getValues)
                    .collect(toMap(Entry::getKey, Entry::getValue)));
            List<CheckPointMatcher<E>> cpMatchers = IntStream.range(0, checkPoints.size())
                    .mapToObj(cpIndex -> {
                        CheckPoint cp = checkPoints.get(cpIndex);
                        Map<String, Value<E>> valuesToStore = mapper.getValues(cp)
                                .collect(toMap(Entry::getKey, Entry::getValue));
                        return new CheckPointMatcher<E>(seqIndex, cpIndex, cp, env, matcherParser,
                                started, context, valuesToStore);
                    })
                    .collect(toList());
            SequenceProcessor<E> processor = new ContainerSequenceProcessor<E>();
            // TODO add factory for different SequenceProcessors
            processor.setCheckPointMatchers(cpMatchers);
            processor.setListeners(started.sequenceListeners);
            processor.setSequence(sequence);
            processor.setContext(context);
            return processor;
        }).collect(Collectors.collectingAndThen(toList(), CopyOnWriteArrayList<SequenceProcessor<E>>::new));
        processors.parallelStream().forEach(processor -> processor.beforeStart());
        try {
            return startProcessing(processors);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        }
    }

    private List<SequenceResult> startProcessing(CopyOnWriteArrayList<SequenceProcessor<E>> processors)
            throws InterruptedException {
        TimeoutChecker timeout = new TimeoutChecker(started);
        completionStrategy.beforeStart(new Processors(processors));
        while (!completionStrategy.isCompleted()) {
            if (timeout.hasOccured()) {
                processors.parallelStream().forEach(p -> p.onTimeout());
                break;
            }
            E event = cachedEvents.getQueue().poll(500, MILLISECONDS);
            if (event != null) {
                processors.parallelStream().forEach(p -> p.process(event));
            }
        }
        return createSequenceResults(started.progressListener);
    }

    private List<SequenceResult> createSequenceResults(ProgressSequenceProcessorListener<E> progressListener) {
        final List<SequenceResult> result = new ArrayList<>();
        for (SequenceStatus s : progressListener.getStatus()) {
            String name = s.getName().toString();
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
