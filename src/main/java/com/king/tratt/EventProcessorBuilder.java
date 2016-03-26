package com.king.tratt;

import static com.king.tratt.Tratt.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Stoppable;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.CheckPoint;
import com.king.tratt.tdl.Tdl;
import com.king.tratt.tdl.TdlBuilder;

/**
 * Let's you configure the behavior of a EventProcessor.
 */
public class EventProcessorBuilder {
    final TdlBuilder tdlBuilder = Tdl.newBuilder();
    final List<Stoppable> stoppables = new ArrayList<>();
    final List<SequenceProcessorListener> sequenceListeners = new ArrayList<>();
    final BlockingQueue<Event> pipeline = new LinkedBlockingQueue<>();
    final List<EventIterator> eventIterators = new ArrayList<>();
    final List<SimpleProcessor> simpleProcessors = new ArrayList<>();
    long timeoutSeconds = 900;
    PipelineProducerStrategy producerStrategy = PipelineProducerStrategy.getDefault();
    boolean tdlValidationEnabled = true;
    ValueFactory valueFactory;
    EventMetaDataFactory metaDataFactory;
    CompletionStrategy completionStrategy;


    public EventProcessorBuilder() {
        /* for package private usage only */
    }

    /**
     *
     * @param provider
     * @return
     */
    public EventProcessorBuilder setValueFactory(ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
        return this;
    }

    public EventProcessorBuilder setEventMetaDataFatory(EventMetaDataFactory mdFactory) {
        this.metaDataFactory = mdFactory;
        return this;
    }

    public EventProcessorBuilder addEventIterator(EventIterator eventIterator) {
        eventIterators.add(eventIterator);
        return this;
    }

    public EventProcessorBuilder addSimpleProcessor(SimpleProcessor simpleProcessor) {
        simpleProcessors.add(simpleProcessor);
        return this;
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
     * Add a variable into the provided TDL file.
     * (provided with {@link #useTdl(TdlFile)} or {@link #useTdl(TdlBuilder)})
     * <p>
     * if the variable already exists in the TDL, it will be overwritten.
     *
     * @param name of variable.
     * @param value of variable.
     * @return this builder
     */
    public EventProcessorBuilder addVariable(String name, Long value) {
        tdlBuilder.addVariable(name, value);
        return this;
    }

    /**
     * Add a variable into the provided TDL file.
     * (provided with {@link #useTdl(TdlFile)} or {@link #useTdl(TdlBuilder)}).
     * <p>
     * if the variable already exists in the TDL, it will be overwritten.
     *
     * @param name of variable.
     * @param value of variable.
     * @return this builder
     */
    public EventProcessorBuilder addVariable(String name, Integer value) {
        tdlBuilder.addVariable(name, value);
        return this;
    }

    /**
     * Add a variable into the provided TDL file.
     * (provided with {@link #useTdl(TdlFile)} or {@link #useTdl(TdlBuilder)})
     * <p>
     * if the variable already exists in the TDL, it will be overwritten.
     *
     * @param name of variable.
     * @param value of variable.
     * @return this builder
     */
    public EventProcessorBuilder addVariable(String name, String value) {
        tdlBuilder.addVariable(name, value);
        return this;
    }

    /**
     * Prepend the given {@code match} expression on all available
     * {@link CheckPoint} match fields.
     *
     * @param match
     *            expression. Example: "coreUserId == $coreUserId" //TODO
     * @return this builder
     */
    public EventProcessorBuilder addMatch(String match) {
        // TODO remove?
        //        tdlBuilder.addMatch(match);
        return this;
    }

    /**
     * Add any TDL file(s) to use. Multiple added TDL files will be merged into
     * one TDL file, before event processing is started.
     *
     * @param first TDL
     * @param rest additional TDL
     * @return
     */
    public EventProcessorBuilder addTdls(Tdl first, Tdl... rest) {
        tdlBuilder.addTdls(util.concat(first, rest));
        return this;
    }

    public EventProcessorBuilder addTdls(List<Tdl> tdls) {
        tdlBuilder.addTdls(tdls);
        return this;
    }

    /**
     * Set a {@link Preprocessor} if such has been used. The
     * {@link Preprocessor} contains a cache of events. Can for example be used
     * if you want to cache the events that happens during installation of your
     * app.
     *
     * @param preprocessor
     * @return this builder TODO Preprocessor
     */
    //    public EventProcessorBuilder setPreprocessor(final Preprocessor preprocessor) {
    /*
     * Set all pre-processor specifics here.
     */

    //        statisticsDataHolder.merge(preprocessor.statisticsDataHolder);
    //        pipeline = preprocessor.eventCacher.getQueue();
    //        stoppables.add(new Stoppable() {
    //
    //            @Override
    //            public void stop() {
    //                preprocessor.shutdown();
    //            }
    //        });
    // To avoid the nonEmptyEventIterators check to fire.
    //        addEventIterator(thatStopsImmediately());
    //    return this;
    //    }

    /**
	 * For experimental usage only! Be aware that method signature will change
	 * without any notice. TODO
	 * 
	 * @param listener
	 * @return this builder
	 */
    public EventProcessorBuilder addProcessorListener(SequenceProcessorListener listener) {
		sequenceListeners.add(listener);
		return this;
	}
	
    public EventProcessorBuilder addCompletionStrategy(CompletionStrategy strategy) {
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
        if (duration < 0) {
            String message = "Argument 'duration' is negative.";
            throw new IllegalArgumentException(message);
        }
        if (timeUnit == null) {
            throw util.nullArgumentError("timeUnit");
        }
        timeoutSeconds = timeUnit.toSeconds(duration);
        return this;
    }

    /**
     * disables TDL validation when starting the EventProcessor.
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

}
