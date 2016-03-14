package com.king.tratt;

import static com.king.tratt.Tratt.util;

import java.util.ArrayList;
import java.util.List;
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

import com.king.tratt.metadata.spi.Event;
import com.king.tratt.metadata.spi.EventMetaDataFactory;
import com.king.tratt.metadata.spi.Stoppable;
import com.king.tratt.metadata.spi.ValueFactory;
import com.king.tratt.tdl.Tdl;

public class StartedEventProcessor<E extends Event> {

    private static Logger logger = LoggerFactory.getLogger(StartedEventProcessor.class);
    private static String ERROR_MESSAGE = "Sequence '%s' is not valid due to: %s \n";
    //    final String requestId;
    private Future<CompletedEventProcessor> tdlProcessorResults;
    final ExecutorService executor;
    final Tdl tdl;
    private final List<EventIterator<E>> eventIterators;
    //    private final StatisticsDataHolder statsDataHolder;
    private final PipelineProducerStrategy<E> producerStrategy;
    private final List<Stoppable> stoppables;
    private final BlockingQueue<E> pipeline;
    private final boolean tdlValidationEnabled;
    final long timeoutSeconds;
    //    private final String trackingToolUrl;
    private final ArrayList<SimpleProcessor<E>> simpleProcessors;
    final EventMetaDataFactory<?> metadataFactory;
    final ValueFactory<E> valueFactory;
    final List<SequenceProcessorListener<E>> sequenceListeners;
    final ProgressSequenceProcessorListener<E> progressListener;
    CompletionStrategy<E> completionStrategy;


    StartedEventProcessor(EventProcessorBuilder<E> builder) {
        // first copy...
        pipeline = builder.pipeline; // don't copy!
        metadataFactory = builder.metaDataFactory;
        valueFactory = builder.valueFactory;
        tdlValidationEnabled = builder.tdlValidationEnabled;
        //        trackingToolUrl = builder.trackingToolUrl;
        timeoutSeconds = builder.timeoutSeconds;
        eventIterators = new ArrayList<>(builder.eventIterators);
        simpleProcessors = new ArrayList<>(builder.simpleProcessors);
        producerStrategy = builder.producerStrategy;
        tdl = builder.tdlBuilder.build();
        sequenceListeners = new ArrayList<>(builder.sequenceListeners);
        stoppables = new ArrayList<>(builder.stoppables);
        completionStrategy = builder.completionStrategy;
        progressListener = new ProgressSequenceProcessorListener<E>(tdl.getSequences());
        sequenceListeners.add(progressListener);
        sequenceListeners.add(new ProcessorLogger<E>());
        //        statsDataHolder = StatisticsDataHolder.copyOf(builder.statisticsDataHolder);
        //        requestId = builder.requestId == null ? valueOf(nanoTime()) : builder.requestId;

        // ...then check invariants
        // TODO chack metadataFactory, valueFactory, etc!
        if (completionStrategy == null) {
            completionStrategy = progressListener;
        }
        //        checkEventIteratorsNotEmpty(iterators);
        if (tdlValidationEnabled) {
            validateTdl(tdl);
        }

        logger.debug("Actual used TDL:\n" + tdl);
        logger.debug("Timeout set to: " + timeoutSeconds + " seconds.");
        // TODO log more stuff here?
        //        if (tdl.getSequences().isEmpty() && processors.isEmpty()) {
        //            String message = "No Sequences in TDL and There are no event-processors to run. "
        //                    + "Either use a tdl-file or enable the console logger.";
        //            throw new IllegalStateException(message);
        //        }

        executor = util.newThreadPool();
        stoppables.add(() -> executor.shutdownNow());
    }

    StartedEventProcessor<E> start() {
        CachedProcessor<E> cachedEvents = util.startProcessingEventsAndCreateCach(
                pipeline, eventIterators, stoppables, simpleProcessors, producerStrategy, executor);
        if (!tdl.getSequences().isEmpty()) {
            //            listeners.add(new LoggingProcessListener(tdl));
            //            listeners.add(new StatisticsProcessListener(requestId, statsDataHolder,
            //                    tdl, trackingToolUrl, executor));
            tdlProcessorResults = newFutureTask(new TdlProcessor<E>(cachedEvents, this));
        } else {
            tdlProcessorResults = newInvalidCompletedFuture();
        }
        return this;
    }

    private void validateTdl(Tdl tdl) {
        TdlValidator<E> validator = new TdlValidator<E>(valueFactory, metadataFactory, tdl);
        if (validator.getError() != null) {
            TdlValidationResult result = new TdlValidationResult(false, validator.getError(),
                    validator.getFieldErrorDescriptors());
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

    private synchronized Future<CompletedEventProcessor> newFutureTask(TdlProcessor<E> tdlProcessor) {
        Callable<CompletedEventProcessor> r = () -> {
                List<SequenceResult> results = tdlProcessor.processTdl(tdl);
                return new CompletedEventProcessor(results);
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
        util.shutdownStoppablesAndExecutorService(stoppables, executor);
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
