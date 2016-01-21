package com.king.tratt;

import static com.king.tracking.api.ProcessorHelper.checkEventIteratorsNotEmpty;
import static com.king.tracking.api.ProcessorHelper.shutdownStoppablesAndExecutorService;
import static com.king.tracking.api.ProcessorHelper.startProcessingEventsAndCreateEventCacher;
import static java.lang.String.valueOf;
import static java.lang.System.nanoTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.king.event.Event;
import com.king.tracking.api.Processors.EventCacher;
import com.king.tracking.eventtracker.event.KafkaEventValueProvider;
import com.king.tracking.eventtracker.kafka.Stoppable;
import com.king.tracking.eventtracker.processor.ProcessListener;
import com.king.tracking.eventtracker.processor.Processor;
import com.king.tracking.metadata.EventMetaData;
import com.king.tracking.metadata.EventMetaDataProvider;
import com.king.tracking.metadata.FieldMetaData;
import com.king.tracking.reducer.ValueProvider;
import com.king.tracking.tdl.Tdl;
import com.king.tracking.tdl.TdlFileValidator;
import com.king.tracking.validationtool.InvalidTdlException;
import com.king.tracking.validationtool.TdlValidationToolResult;
import com.king.tratt.spi.EventIterator;

public class StartedEventProcessor {

    private static Logger logger = LoggerFactory.getLogger(StartedEventProcessor.class);
    private static String ERROR_MESSAGE = "Sequence '%s' is not valid due to: %s \n";
    final String requestId;
    private final Future<CompletedEventProcessor> tdlProcessorResults;
    private final ExecutorService executor;
    final Tdl tdl;
    private final List<ProcessListener<Event>> listeners;
    private final Set<EventIterator<Event>> iterators;
    private final StatisticsDataHolder statsDataHolder;
    private final Map<Object, Processor<Event>> processors;
    private final PipelineProducerStrategy<Event> producerStrategy;
    private final List<Stoppable> stoppables;
    private final BlockingQueue<Event> pipeline;
    private final boolean tdlValidationEnabled;
    private final long timeoutSeconds;
    private final String trackingToolUrl;
    private final EventMetaDataProvider<FieldMetaData, EventMetaData<FieldMetaData>> metadataProvider;


    public StartedEventProcessor(EventProcessorBuilder builder) {
        // first copy...
        pipeline = builder.pipeline; // don't copy!
        metadataProvider = builder.metadataProvider;
        tdlValidationEnabled = builder.tdlValidationEnabled;
        trackingToolUrl = builder.trackingToolUrl;
        timeoutSeconds = builder.timeoutSeconds;
        iterators = new HashSet<>(builder.iterators);
        processors = new HashMap<>(builder.processors);
        producerStrategy = builder.producerStrategy;
        tdl = builder.tdlBuilder.build();
        listeners = new ArrayList<>(builder.listeners);
        stoppables = new ArrayList<>(builder.stoppables);
        statsDataHolder = StatisticsDataHolder.copyOf(builder.statisticsDataHolder);
        requestId = builder.requestId == null ? valueOf(nanoTime()) : builder.requestId;

        // ...then check invariants
        checkEventIteratorsNotEmpty(iterators);
        if (tdlValidationEnabled) {
            validateTdl(tdl);
        }

        logger.debug("Actual used TDL:\n" + tdl);
        logger.debug("Timeout set to: " + timeoutSeconds + " seconds.");
        if (tdl.getSequences().isEmpty() && processors.isEmpty()) {
            String message = "No Sequences in TDL and There are no event-processors to run. "
                    + "Either use a tdl-file or enable the console logger.";
            throw new IllegalStateException(message);
        }

        executor = ProcessorHelper.newThreadPool(requestId);
        stoppables.add(new Stoppable() {

            @Override
            public void stop() {
                executor.shutdownNow();
            }
        });

        EventCacher<Event> eventCacher = startProcessingEventsAndCreateEventCacher(
                pipeline, iterators, stoppables, producerStrategy, processors, executor);
        if (!tdl.getSequences().isEmpty()) {
            listeners.add(new LoggingProcessListener(tdl));
            listeners.add(new StatisticsProcessListener(requestId, statsDataHolder,
                    tdl, trackingToolUrl, executor));
            KafkaTdlProcessor tdlProcessor = new KafkaTdlProcessor(eventCacher, listeners, executor, timeoutSeconds);
            tdlProcessorResults = newFutureTask(tdlProcessor, tdl);
        } else {
            tdlProcessorResults = newInvalidCompletedFuture();
        }
    }

    private void validateTdl(Tdl tdl) {
        ValueProvider<Event> valueProvider = new KafkaEventValueProvider(metadataProvider);
        TdlFileValidator validator = new TdlFileValidator(valueProvider, metadataProvider, tdl, true);
        if(validator.getError() != null){
            TdlValidationToolResult result = new TdlValidationToolResult(false, validator.getError(), validator.getFieldErrorDescriptors());
            throw new InvalidTdlException(result);
        }
    }

    private Future<CompletedEventProcessor> newInvalidCompletedFuture() {
        return new Future<CompletedEventProcessor>() {
            CompletedEventProcessor completed = new CompletedEventProcessor();

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return true;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public CompletedEventProcessor get() throws InterruptedException, ExecutionException {
                return completed;
            }

            @Override
            public CompletedEventProcessor get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
                return get();
            }

        };
    }

    private synchronized Future<CompletedEventProcessor> newFutureTask(final KafkaTdlProcessor tdlProcessor,
            final Tdl tdlFile) {
        Callable<CompletedEventProcessor> r = new Callable<CompletedEventProcessor>() {

            @Override
            public CompletedEventProcessor call() {
                List<SequenceResult> results = tdlProcessor.processTdl(tdlFile);
                return new CompletedEventProcessor(results);
            }
        };
        FutureTask<CompletedEventProcessor> task = new FutureTask<CompletedEventProcessor>(r);
        new Thread(task).start();
        return task;
    }

    /**
     * Waits until all Sequences have successfully ended. All EventProcessors are automatically
     * shutdown before this method returns.
     * <p>
     * If no Sequences are running, this method returns immediately (for example if only the console
     * logger is enabled) and EventProcessors are not shutdown.
     *
     * @throws TdlFailureException when at least one EventProcessor (Sequence) fails or times out.
     */
    public void awaitSuccess() {
        CompletedEventProcessor completed = awaitCompletion();

        if (completed.isValid() || completed.getSequenceResults().isEmpty()) {
            return;
        }

        StringBuilder errorMessage = new StringBuilder();
        for (SequenceResult result : completed.getSequenceResults()) {
            if (!result.isValid()) {
                errorMessage.append(String.format(ERROR_MESSAGE, result.getName(), result.getCauses()));
            }
        }
        throw new TdlFailureException(errorMessage.toString());
    }

    /**
     * Waits until all Sequences have ended. All EventProcessors are automatically
     * shutdown before this method returns.
     * <p>
     * If no Sequences are running, this method returns immediately (for example if only the console
     * logger is enabled) and EventProcessors are not shutdown.
     *
     * @throws TimeoutException when at least one EventProcessor (Sequence) fails, or
     *         a timeout occurs.
     */
    public CompletedEventProcessor awaitCompletion() {
        try {
            // blocking call.
            return tdlProcessorResults.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        } finally {
            shutdown();
        }
    }

    /**
     * Shutdowns all EventProcessors.
     */
    public void shutdown() {
        shutdownStoppablesAndExecutorService(stoppables, executor);
    }

    /**
     * Returns true if EventProcessor has completed. Use {@link #awaitCompletion()} to findout
     * the result.
     *
     * @return true if EventProcessor is completed, otherwise false.
     */

    public boolean isCompleted() {
        return tdlProcessorResults.isDone();
    }

}
