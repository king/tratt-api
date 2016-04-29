/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.internal.Util.concat;
import static com.king.tratt.internal.Util.requireNonNull;
import static com.king.tratt.internal.Util.requireNoneNegative;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.king.tratt.spi.ApiConfigurationProvider;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Stoppable;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.Tdl;
import com.king.tratt.tdl.TdlBuilder;

/**
 * Let's you configure the behavior of a EventProcessor.
 */
public final class EventProcessorBuilder {
    final TdlBuilder tdlBuilder = Tdl.newBuilder();
    final List<Stoppable> stoppables = new ArrayList<>();
    final List<SequenceProcessorListener> sequenceListeners = new ArrayList<>();
    final List<EventIterator> eventIterators = new ArrayList<>();
    final List<SimpleProcessor> simpleProcessors = new ArrayList<>();
    long timeoutSeconds = 900;
    boolean tdlValidationEnabled = true;
    BlockingQueue<Event> pipeline = new LinkedBlockingQueue<>();
    ValueFactory valueFactory;
    EventMetaDataFactory metaDataFactory;
    CompletionStrategy completionStrategy;
    boolean isPreprocessorUsed = false;

    EventProcessorBuilder() {
        /* for package private usage only */
    }

    /**
     * Starts the EventProcessor that process the events from the Kafka queue.
     *
     * @return
     */
    public StartedEventProcessor start() {
        return new StartedEventProcessor(this).start();
    }

    /**
     *
     * @param provider
     * @return this builder
     */
    public EventProcessorBuilder setApiConfigurationProvider(ApiConfigurationProvider provider) {
        requireNonNull(provider, "provider");
        setValueFactory(provider.valueFactory());
        setEventMetaDataFatory(provider.metaDataFactory());
        return this;
    }

    /**
     *
     * @param provider
     * @return this builder
     */
    public EventProcessorBuilder setValueFactory(ValueFactory valueFactory) {
        requireNonNull(valueFactory, "valueFactory");
        this.valueFactory = valueFactory;
        return this;
    }

    public EventProcessorBuilder setEventMetaDataFatory(EventMetaDataFactory mdFactory) {
        requireNonNull(mdFactory, "mdFactory");
        this.metaDataFactory = mdFactory;
        return this;
    }

    public EventProcessorBuilder addEventIterator(EventIterator eventIterator) {
        requireNonNull(eventIterator, "eventIterator");
        eventIterators.add(eventIterator);
        return this;
    }

    public EventProcessorBuilder addSimpleProcessor(SimpleProcessor simpleProcessor) {
        requireNonNull(simpleProcessor, "simpleProcessor");
        simpleProcessors.add(simpleProcessor);
        return this;
    }

    /**
     * Add a variable into the actual used TDL file.
     * <p>
     * if the variable already exists in the TDL, it will be overwritten.
     *
     * @param name
     *            of variable.
     * @param value
     *            of variable.
     * @return this builder
     */
    public EventProcessorBuilder addVariable(String name, Long value) {
        tdlBuilder.addVariable(name, value);
        return this;
    }

    /**
     * Add a variable into the actual used TDL file.
     * <p>
     * if the variable already exists in the TDL, it will be overwritten.
     *
     * @param name
     *            of variable.
     * @param value
     *            of variable.
     * @return this builder
     */
    public EventProcessorBuilder addVariable(String name, Integer value) {
        tdlBuilder.addVariable(name, value);
        return this;
    }

    /**
     * Add a variable into the actual used TDL file.
     * <p>
     * if the variable already exists in the TDL, it will be overwritten.
     *
     * @param name
     *            of variable.
     * @param value
     *            of variable.
     * @return this builder
     */
    public EventProcessorBuilder addVariable(String name, String value) {
        tdlBuilder.addVariable(name, value);
        return this;
    }

    /**
     * Add any TDL file(s) to use. Multiple added TDL files will be merged into
     * one TDL file, before event processing is started.
     *
     * @param first
     *            TDL
     * @param rest
     *            additional TDL
     * @return
     */
    public EventProcessorBuilder addTdls(Tdl first, Tdl... rest) {
        return addTdls(concat(first, rest));
    }

    public EventProcessorBuilder addTdls(List<Tdl> tdls) {
        tdlBuilder.addTdls(tdls);
        return this;
    }

    /**
     *
     * @param listener
     * @return this builder
     */
    public EventProcessorBuilder addProcessorListener(SequenceProcessorListener listener) {
        requireNonNull(listener, "listener");
        sequenceListeners.add(listener);
        return this;
    }

    EventProcessorBuilder addCompletionStrategy(CompletionStrategy strategy) {
        completionStrategy = strategy;
        return this;
    }

    /**
     * Set max allowed duration for the {@link StartedEventProcessor} as
     * returned from {@link #start()} method. If max allowed duration is passed
     * then the {@link StartedEventProcessor#await*()} methods will throw an
     * exception.
     * <p>
     * Default value is 900 seconds.
     *
     * @param duration
     * @param timeUnit
     *            of duration
     * @return this builder
     */
    public EventProcessorBuilder setTimeout(long duration, TimeUnit timeUnit) {
        requireNoneNegative(duration, "duration");
        requireNonNull(timeUnit, "timeUnit");
        timeoutSeconds = timeUnit.toSeconds(duration);
        return this;
    }

    /**
     * disables TDL validation when starting the EventProcessor.
     *
     * @return this builder
     */
    public EventProcessorBuilder disableTdlValidation() {
        tdlValidationEnabled = false;
        return this;
    }

    /**
     * Set the value of the TDL comment field.
     */
    public EventProcessorBuilder setComment(String comment) {
        tdlBuilder.setComment(comment);
        return this;
    }

    public EventProcessorBuilder setPreprocessor(Preprocessor pre) {
        return setEventCache(pre.eventCache);
    }

    EventProcessorBuilder setEventCache(CachingProcessor eventCache) {
        isPreprocessorUsed = true;
        pipeline = eventCache.blockingQueue;
        return this;
    }
}
