package com.king.tratt;

import static com.king.tratt.Tratt.util;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Stoppable;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.Tdl;

public class StartedEventProcessor {

    private static Logger LOG = LoggerFactory.getLogger(StartedEventProcessor.class);
    private static String ERROR_MESSAGE = "Sequence '%s' is not valid due to: %s \n";
    //    final String requestId;
    private Future<CompletedEventProcessor> tdlProcessorResults;
    final ExecutorService executor;
    final Tdl tdl;
    private final List<EventIterator> eventIterators;
    //    private final StatisticsDataHolder statsDataHolder;
    private final PipelineProducerStrategy producerStrategy;
    private final List<Stoppable> stoppables;
    private final BlockingQueue<Event> pipeline;
    private final boolean tdlValidationEnabled;
    final long timeoutSeconds;
    //    private final String trackingToolUrl;
    private final ArrayList<SimpleProcessor> simpleProcessors;
    final EventMetaDataFactory metadataFactory;
    final ValueFactory valueFactory;
    final List<SequenceProcessorListener> sequenceListeners;
    final ProgressSequenceProcessorListener progressListener;
    CompletionStrategy completionStrategy;


    StartedEventProcessor(EventProcessorBuilder builder) {
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
        progressListener = new ProgressSequenceProcessorListener(tdl.getSequences());
        sequenceListeners.add(progressListener);
        sequenceListeners.add(new ProcessorLogger());
        //        statsDataHolder = StatisticsDataHolder.copyOf(builder.statisticsDataHolder);
        //        requestId = builder.requestId == null ? valueOf(nanoTime()) : builder.requestId;

        // ...then check invariants
        // TODO check mandatory data metadataFactory, valueFactory, etc!
        if (completionStrategy == null) {
            completionStrategy = progressListener;
        }
        checkNotNull(metadataFactory, valueFactory);
        checkEventIteratorsNotEmpty();
        if (tdlValidationEnabled) {
            validateTdl(tdl);
        }
        if (tdl.getSequences().isEmpty() && simpleProcessors.isEmpty()) {
            String message = "No Sequences in TDL and There are no simple-processors to run. "
                    + "Either add a tdl-file or add a simple-processor.";
            throw new IllegalStateException(message);
        }

        // Log stuff here TODO log more stuff?
        LOG.debug("Actual used TDL:\n" + tdl);
        LOG.debug("Timeout set to: " + timeoutSeconds + " seconds.");
        LOG.debug("tdlValidationEnabled: " + tdlValidationEnabled);

        executor = util.newThreadPool();
        stoppables.add(() -> executor.shutdownNow());
    }

    private void checkNotNull(Object... objects) {
        Arrays.stream(objects).filter(o -> o == null).findFirst().ifPresent(o -> {
            String template = "No %s set.";
            throw new IllegalStateException(String.format(template, o.getClass().getSimpleName()));
        });
        if (metadataFactory == null) {
            String message = "No %s set.";
            message = String.format(message, EventMetaDataFactory.class.getSimpleName());
            throw new IllegalStateException(message);
        }
    }

    StartedEventProcessor start() {
        CachedProcessor cachedEvents = util.startProcessingEventsAndCreateCach(
                pipeline, eventIterators, stoppables, simpleProcessors, producerStrategy, executor);
        if (!tdl.getSequences().isEmpty()) {
            //            listeners.add(new StatisticsProcessListener(requestId, statsDataHolder,
            //                    tdl, trackingToolUrl, executor));
            tdlProcessorResults = newFutureTask(new TdlProcessor(cachedEvents, this));
        } else {
            tdlProcessorResults = newInvalidCompletedFuture();
        }
        return this;
    }

    void checkEventIteratorsNotEmpty() {
        if (eventIterators.isEmpty()) {
            String message = "No EventIterators added!";
            throw new IllegalStateException(message);
        }
    }

    private void validateTdl(Tdl tdl) {
        TdlValidator validator = new TdlValidator(valueFactory, metadataFactory, tdl);
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

    private synchronized Future<CompletedEventProcessor> newFutureTask(TdlProcessor tdlProcessor) {
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
