// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;
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
import java.util.function.Function;
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

public final class InternalUtil {
    private static final Logger LOG = LoggerFactory.getLogger(InternalUtil.class);
    private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", CASE_INSENSITIVE);
    private static final Pattern CONVERSION_PATTERN = Pattern.compile("(~[vdsq])");
    private static final AtomicInteger counter = new AtomicInteger();

    InternalUtil() {
        /* For package private usage only */
    }

    public Object parseSupportedType(String value) {
        return parseValue(value, l -> l, s -> s, b -> b);
    }

    String quoted(Object value) {
        return parseValue(value, String::valueOf,
                s -> String.format("'%s'", s), String::valueOf);
    }

    <T> T parseValue(Object value, Function<Long, T> longFunc,
            Function<String, T> strFunc, Function<Boolean, T> boolFunc) {
        if (value instanceof Long) {
            return longFunc.apply((Long) value);
        } else if (value instanceof Boolean) {
            return boolFunc.apply((Boolean) value);
        } else if (value instanceof String) {
            String str = (String) value;
            if (util.isLong(str)) {
                return longFunc.apply(parseLong(str));
            } else if (util.isBoolean(str)) {
                return boolFunc.apply(parseBoolean(str));
            }
            return strFunc.apply(str);
        }
        String message = "Unsupported type for Value.get(...): '%s' [%s]";
        throw new IllegalStateException(String.format(message,
                value.getClass(), value));
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
     * '~v' (v as in get Value)
     * '~s' (s as in toString)
     * '~q' (q as in quoted value (quoted if applicable)).
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

    private String doConversion(Event e, Context context, String action, Object o) {
        switch (action) {
        case "~v":
            return ((Value) o).asString(e, context);
        case "~d":
            return ((DebugStringAware) o).toDebugString(e, context);
        case "~s":
            return o.toString();
        case "~q":
            return quoted(((Value) o).get(e, context));
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
