/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.Tratt.values;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
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
import com.king.tratt.spi.Value;

class InternalUtil {
    private static final Logger LOG = LoggerFactory.getLogger(InternalUtil.class);
    private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", CASE_INSENSITIVE);
    private static final Pattern CONVERSION_PATTERN = Pattern.compile("(~[gdsp])");
    private static final AtomicInteger counter = new AtomicInteger();

    InternalUtil() {
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

    String formatJoin(Event e, Context context, String glue, String format,
            List<? extends Object> args) {
        return args.stream()
                .map(o -> format(e, context, format, o))
                .collect(joining(glue));
    }

    /**
     * The 'format' string can contain the following conversion patterns, where
     * each conversion pattern corresponds to an object in the args list (in
     * order. e.g first conversion pattern found in 'format' corresponds to
     * first element in args and so on).
     *
     * <pre>
     * '~d' (d as in debug)
     * '~g' (g as in get)
     * '~s' (s as in string)
     * '~p' (p as in plain) TODO change to 'q'?
     * </pre>
     *
     * check the below switch case to see what the conversion patterns do.
     *
     * @return a formatted string
     */
    String format(final Event event, final Context context, String format, Object... args) {
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

    // TODO : change "g" to "v"; v as in value
    private String doConversion(Event e, Context context, String action, Object o) {
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
        return newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "TRATT." + counter.incrementAndGet());
            thread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
                String message = "Thread '%s' crashed unexpectedly due to:\n";
                LOG.error(String.format(message, t), e);
            });
            return thread;
        });
    }

    CachingProcessor startProcessingEventsAndCreateCache(
            BlockingQueue<Event> pipeline, List<EventIterator> eventIterators,
            List<Stoppable> stoppables, List<SimpleProcessor> simpleProcessors,
            ExecutorService executor) {

        for (EventIterator eventIterator : eventIterators) {
            // Start iterating over event stream and add events into event
            // pipeline.
            eventIterator.start();
            executor.execute(new PipelineProducerFromEventIterator(eventIterator, pipeline));
            stoppables.add(eventIterator);
        }

        // Multicast all events from pipeline to added simpleProcessors.
        CachingProcessor cachedEvents = new CachingProcessor();
        PipelineEventMulticaster pipelineConsumer = new PipelineEventMulticaster(pipeline);
        pipelineConsumer.addProcessors(simpleProcessors);
        pipelineConsumer.addProcessor(cachedEvents);
        executor.execute(pipelineConsumer);
        return cachedEvents;
    }

    void shutdownStoppablesAndExecutorService(List<Stoppable> stoppables,
            ExecutorService executor) {
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
