package com.king.tratt;

import static com.king.tratt.Tratt.values;
import static java.lang.ClassLoader.getSystemResource;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.DebugStringAware;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.Stoppable;
import com.king.tratt.spi.SufficientContextAware;
import com.king.tratt.spi.Value;

class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", CASE_INSENSITIVE);
    private static final String CLASSPATH_PROTOCOL = "classpath:";
    private static final String FILE_PROTOCOL = "file:";

    Util() {
        /* For package private usage only */
    }


    /*
     * Check if a String can be parsed to a 'Long'.
     */
    boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /*
     * Check if a String can be parsed to a 'Boolean'.
     */
    boolean isBoolean(String nodeValue) {
        return IS_BOOLEAN.matcher(nodeValue).matches();
    }

    <E extends Event> boolean hasSufficientContext(Context context,
            List<? extends SufficientContextAware> awares) {
        for (SufficientContextAware aware : awares) {
            if (!aware.hasSufficientContext(context)) {
                return false;
            }
        }
        return true;
    }

    <T> List<T> concat(T first, @SuppressWarnings("unchecked") T... rest) {
        return concat(first, asList(rest));
    }

    <T> List<T> concat(T value, List<T> values) {
        List<T> list = new ArrayList<>();
        list.add(value);
        list.addAll(values);
        return list;
    }

    NullPointerException nullArgumentError(String name) {
        String message = "Argument '%s' is null.";
        return new NullPointerException(String.format(message, name));
    }

    IllegalArgumentException varArgError(String... args) {
        return varArgError((Object[]) args);
    }

    IllegalArgumentException varArgError(Object... args) {
        String message = "One of the var-args is either null or empty: %s";
        String arraysToString = Arrays.toString(args);
        return new IllegalArgumentException(String.format(message, arraysToString));
    }

    IllegalArgumentException emptyStringArgumentError(String name) {
        String message = "Argument '%s' is empty string.";
        return new IllegalArgumentException(String.format(message, name));
    }

    /**
     * Takes a protocol prefixed path. Accepted protocols are "file" and "classpath".
     * Example:
     * "classpath:root-folder/file.txt"
     * "file:/root/file.txt"
     * "file:/c:/temp/file.txt"
     * No prefix works as well, and will be used as: new File("path").
     */
    Path toPath(String prefixedPath) {
        try {
            URI uri;
            if (prefixedPath.startsWith(CLASSPATH_PROTOCOL)) {
                String stringPath = prefixedPath.substring(CLASSPATH_PROTOCOL.length());
                uri = getSystemResource(stringPath).toURI();
            } else if (prefixedPath.startsWith(FILE_PROTOCOL)) {
                uri = new URL(prefixedPath).toURI();
            } else {
                uri = new File(prefixedPath).toURI();
            }
            return Paths.get(uri);
        } catch (Exception e) {
            throw new IllegalArgumentException(prefixedPath, e);
        }

    }

    <E extends Event> String formatJoin(E e, Context context, String glue, String format,
            List<? extends Object> args) {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        // TODO use join Java8 feature?
        for (Object o : args) {
            sb.append(separator);
            separator = glue;
            sb.append(format(e, context, format, o));
        }
        return sb.toString();
    }

    /**
     * The 'format' string can contain the following conversion patterns, where each conversion
     * pattern corresponds to an object in the args list (in order. e.g first conversion pattern
     * found * in 'format' corresponds to first element in args and so on).
     * '~d' (d as in debug):
     * '~g' (g as in get):
     * '~s' (s as in string):
     * '~p' (p as in plain):
     * check the below switch case to see what the conversion patterns do.
     *
     * @return a formatted string
     */
    private static final Pattern CONVERSION_PATTERN = Pattern.compile("(~[gdsp])");

    <E extends Event> String format(final E event, final Context context, String format, Object... args) {
        List<Object> replacements = new ArrayList<>(asList(args));
        Matcher m = CONVERSION_PATTERN.matcher(format);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String conversion = m.group();
            Object arg = replacements.remove(0);
            String replacement = doConversion(event, context, conversion, arg);
            m.appendReplacement(sb, quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    //TODO : change "g" to "v"; v as in value
    private <E extends Event> String doConversion(E e, Context context, String action, Object o) {
        switch (action) {
        case "~g":
            return ((Value) o).asString(e, context);
        case "~d":
            return ((DebugStringAware) o).toDebugString(e, context);
        case "~s":
            return o.toString();
        case "~p":
            return values.quoted(((Value) o).get(e, context));
        default:
            String message = "Unsupported conversion: '%s'";
            throw new IllegalStateException(String.format(message, action));
        }
    }

    ExecutorService newThreadPool() {
        AtomicInteger counter = new AtomicInteger();
		return newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "TRATT." + counter.incrementAndGet());
            thread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
                String message = "Thread '%s' crashed unexpectedly due to:\n";
                LOG.error(String.format(message, t), e);
            });
            return thread;
		});
	}

    <E extends Event> CachedProcessor startProcessingEventsAndCreateCach(
            BlockingQueue<Event> pipeline, List<EventIterator> eventIterators,
            List<Stoppable> stoppables, List<SimpleProcessor> simpleProcessors,
            PipelineProducerStrategy producerStrategy, ExecutorService executor) {

        for (EventIterator eventIterator : eventIterators) {
            // Create Producer and start producing events to the eventPipeline.
            eventIterator.start();
            executor.submit(new PipelineProducer<>(eventIterator, pipeline, producerStrategy));
            stoppables.add(eventIterator);
        }

        // Create consumer and add processors to forward events to, then start consuming the pipeline.
        CachedProcessor cachedEvents = new CachedProcessor();
        PipelineConsumer pipelineConsumer = new PipelineConsumer(pipeline);
        pipelineConsumer.addProcessor(cachedEvents);
        for (SimpleProcessor processor : simpleProcessors) {
            pipelineConsumer.addProcessor(processor);
        }
        executor.submit(pipelineConsumer);
        return cachedEvents;
    }

    // EventCacher<Event> startProcessingEventsAndCreateEventCacher(
    //            BlockingQueue<Event> eventPipeline,
    //            Set<EventIterator<Event>> eventIterators, List<Stoppable> stoppables,
    //            PipelineProducerStrategy<Event> producerStrategy, Map<Object, Processor<Event>> eventProcessors,
    //            ExecutorService executor) {
    //
    // CachingEventProcessor eventCacher = Processors.eventCacher();
    //
    //        PipelineProducer<Event> producer;
    //        for (EventIterator<Event> eventIterator : eventIterators) {
    //            // Create Producer and start producing events to the eventPipeline.
    //            producer = new PipelineProducer<>(eventIterator, eventPipeline, producerStrategy, executor);
    //            producer.start();
    //            stoppables.add(producer);
    //        }
    //
    //        // Create consumer and add processors to forward events to, then start consuming the pipeline.
    //        PipelineConsumer<Event> pipelineConsumer = new PipelineConsumer<>(eventPipeline, executor);
    //        pipelineConsumer.addProcessor(eventCacher);
    //        for (Processor<Event> processor : eventProcessors.values()) {
    //            pipelineConsumer.addProcessor(processor);
    //        }
    //        pipelineConsumer.start();
    //
    //        return eventCacher;
    //    }

    void shutdownStoppablesAndExecutorService(List<Stoppable> stoppables, ExecutorService executor) {
        for (Stoppable stopable : stoppables) {
            stopable.stop();
        }
        try {
            executor.awaitTermination(5, SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
