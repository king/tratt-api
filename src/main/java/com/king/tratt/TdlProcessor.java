package com.king.tratt;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.king.tracking.api.ProgressListener;
import com.king.tracking.api.SequenceResult;
import com.king.tracking.eventtracker.processor.Processor;
import com.king.tracking.eventtracker.processor.TimeoutAwareProcessor;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.CheckPoint;
import com.king.tratt.tdl.Tdl;


class TdlProcessor<E extends Event> {

    private final CachedProcessor<E> cachedEvents;
    private final ValueFactory<E> valueFactory;
    private final EventMetaDataFactory<?> metadataFactory;

    public TdlProcessor(CachedProcessor<E> cachedEvents, ValueFactory<E> valueFactory,
            EventMetaDataFactory<?> metadataFactory) {
        this.cachedEvents = cachedEvents;
        this.valueFactory = valueFactory;
        this.metadataFactory = metadataFactory;
    }

    public List<SequenceResult> processTdl(Tdl tdl) {
        MatcherParser<E> matcherParser = new MatcherParser<>(valueFactory);

        //        Storage<StreamKey, StateContext> storage = new StateContextStorage();
        //        EventMetaDataProvider<FieldMetaData, EventMetaData<FieldMetaData>> metadataProvider = new FromClassEventMetaDataProvider();
        //        NodeParser nodeParser = new TdlNodeParser();
        //        ValueProvider<Event> valueProvider = new KafkaEventValueProvider(metadataProvider);
        //        FunctionProvider<Event> functionProvider = new FunctionProviderImpl<>();
//        @SuppressWarnings("unchecked")
        //        MatcherParser<Event> matcherParser = new MatcherParserImpl<>(nodeParser, valueProvider, functionProvider);
        //        ContextImp context = new ContextImp();
        //        ContextProvider contextProvider = new ContextProviderImpl();
        //        TimeParser<Event> timeParser = new EventTimeParser();

        Map<String, String> tdlVariables = VariableParser.parse(tdl.getVariables());
        tdl.getSequenceInvariants(); // TO DO parse? / Remove at the moment?


        List<SequenceProcessor<E>> processors = tdl.getSequences().stream()
                .<SequenceProcessor<E>> map(sequence -> {
                    List<CheckPoint> checkPoints = sequence.getCheckPoints();
                    Environment<E> env = new Environment<E>(tdlVariables);
                    env.localVariables.putAll(checkPoints.stream()
                            .flatMap(cp -> extractLocalVariables(cp))
                            .collect(toMap(Entry::getKey, Entry::getValue)));

                    List<CheckPointMatcher<E>> cpMatchers = IntStream.range(0, checkPoints.size())
                            .mapToObj(i -> new CheckPointMatcher<E>(i, checkPoints.get(i), env))
                            .collect(toList());

                    SequenceProcessor<E> processor = new ContainerProcessor<E>();
                    // TODO add factory for different SequenceProcessors
                    //                    if (sequence.getType().equals(Sequence.Type.CONTAINER)) {
                    //                        processor = new ContainerProcessor<E>();
                    //                    }
                    processor.setCheckPointMatchers(cpMatchers);
                    processor.setListeners();
                    return processor;
                })
                .collect(Collectors.toList());
        System.out.println("HELLO!");
		return startProcessing();;

        //        EventProcessorProvider eventProcessorProvider = new EventProcessorProvider(storage,
        //                matcherParser,
        //                valueProvider, contextProvider,
        //                new CopyOnWriteArrayList<Processor<Event>>(), metadataProvider, timeParser);
        //        try {
        //            ProgressListener progressListener = createProgressListener(tdl);
        //            processListeners.add(progressListener);
        //            processListeners.add(this);
        //            processors = parseProcessors(tdl, eventProcessorProvider);
        //            List<SequenceResult> response = process(progressListener, tdl);
        //            return response;
        //        } catch (Exception e) {
        //            throw new IllegalArgumentException(e);
        //        }
    }

    private List<SequenceResult> process(ProgressListener progressListener, Tdl tdl)
            throws Exception {
        while (!progressListener.isDone()) {
            Event event = eventCache.getQueue().poll(500, MILLISECONDS);
            if (event != null) {
                process(event);
            }

            if (progressListener.hasTimeoutOccured() && !progressListener.isDone()) {
                for (Processor<?> p : processors) {
                    if (p instanceof TimeoutAwareProcessor) {
                        ((TimeoutAwareProcessor<?>) p).onTimeout();
                    }
                }
            }
        }
        return createSequenceResults(progressListener);
    }

    private List<SequenceResult> startProcessing() {
		// TODO Auto-generated method stub
		return null;
	}

	Stream<Entry<String, Value<E>>> extractLocalVariables(CheckPoint checkPoint) {
        EventMetaData eventMetaData = metadataFactory.getEventMetaData(checkPoint.getEventType());
        List<String> set = checkPoint.getSet();
        return VariableParser.parse(set).entrySet().stream().map(
                entry -> {
                    Value<E> value = Optional.of(valueFactory.getValue(eventMetaData.getName(), entry.getValue()))
                            .orElseGet(() -> tryGetConstantValue(entry.getValue()));
                    return new AbstractMap.SimpleEntry<String, Value<E>>(entry.getKey(), value);
                });
    }

    private Value<E> tryGetConstantValue(final String str) {
        String value = null;
        if (TrattUtil.isLong(str) || TrattUtil.isBoolean(str)) {
            value = str;
        } else if (str.length() < 2) {
            // do nothing
        } else if (str.startsWith("'") && str.endsWith("'")) {
            // remove leading and trailing single quote (')
            value = str.replaceAll("^'|'$", "");
        }
        if (value == null) {
            String message = "Bad set expression '%s'. Valid examples are: myVar=fieldName or myVar='constant123'";
            throw new IllegalStateException(String.format(message, str));
        }
        return Values.constant(value);
    }

    //        EventMetaData eventMetaData = metadataFactory.getEventMetaData(checkPoint.getEventType());
    //        for (String s : set) {
    //            String[] split = s.split("=", 2);
    //            if(split.length < 2){
    //                String message = "Bad set expression '%s'. Must contain a '=' character. '"
    //                        + "Valid examples are: myVar=fieldName or myVar='constant123'";
    //                throw new IllegalStateException(String.format(message, s));
    //            }
    //            String key = split[0].trim();
    //            String literal = split[1].trim();
    //
    //            Value<E> val = valueFactory.getValue(eventMetaData.getName(), literal);
    //            if(val == null){
    //                String str = tryGetConstant(literal);
    //                if (str == null) {
    //                    String message = "Bad set expression '%s'. Valid examples are: myVar=fieldName or myVar='constant123'";
    //                    throw new IllegalStateException(String.format(message, s));
    //                }
    //                val = Values.constant(str);
    //            }
    //            result.put(key, val);
    //        }
    //        return result.entrySet().stream();

    //    private String tryGetConstant(String val) {
    //        if (TrattUtil.isLong(val)) {
    //            return val;
    //        }
    //        if (val.length() < 2) {
    //            return null;
    //        }
    //        if (val.charAt(0) == '\'' && val.charAt(val.length() - 1) == '\'') {
    //            return val.substring(1, val.length() - 2);
    //        }
    //        if (val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {
    //            return val.substring(1, val.length() - 2);
    //        }
    //        return null;
    //    }

}
