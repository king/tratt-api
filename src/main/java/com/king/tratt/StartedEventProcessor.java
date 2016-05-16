/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
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

import com.king.tratt.spi.ApiConfigurationProvider;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.Stoppable;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.Tdl;

public class StartedEventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StartedEventProcessor.class);
    private static final String ERROR_MESSAGE = "Sequence '%s' is not valid due to: %s \n";
    private Future<CompletedEventProcessor> tdlProcessorResults;
    final ExecutorService executor = util.newThreadPool();
    final Tdl tdl;
    private final List<EventIterator> eventIterators;
    private final boolean isPreprocessorUsed;
    private final List<Stoppable> stoppables;
    private final BlockingQueue<Event> pipeline;
    private final boolean tdlValidationEnabled;
    final long timeoutSeconds;
    private final ArrayList<SimpleProcessor> simpleProcessors;
    EventMetaDataFactory metadataFactory;
    ValueFactory valueFactory;
    final List<SequenceProcessorListener> sequenceListeners;
    final ProgressSequenceProcessorListener progressListener;
    CompletionStrategy completionStrategy;

    StartedEventProcessor(EventProcessorBuilder builder) {
        // first copy...
        pipeline = builder.pipeline; // don't copy!
        metadataFactory = builder.metaDataFactory;
        valueFactory = builder.valueFactory;
        tdlValidationEnabled = builder.tdlValidationEnabled;
        isPreprocessorUsed = builder.isPreprocessorUsed;
        timeoutSeconds = builder.timeoutSeconds;
        eventIterators = new ArrayList<>(builder.eventIterators);
        simpleProcessors = new ArrayList<>(builder.simpleProcessors);
        tdl = builder.tdlBuilder.build();
        sequenceListeners = new ArrayList<>(builder.sequenceListeners);
        stoppables = new ArrayList<>(builder.stoppables);
        completionStrategy = builder.completionStrategy;
        progressListener = new ProgressSequenceProcessorListener(tdl.getSequences());

        sequenceListeners.add(progressListener);
        sequenceListeners.add(new ProcessorLogger());

        // ...then check invariants
        if (completionStrategy == null) {
            completionStrategy = progressListener;
        }
        setApiConfigurationIfAvailableFromServiceLoader();
        checkNotNull(metadataFactory, EventMetaDataFactory.class);
        checkNotNull(valueFactory, ValueFactory.class);
        if (!isPreprocessorUsed) {
            checkEventIteratorsNotEmpty();
        }
        if (tdlValidationEnabled) {
            validateTdl(tdl);
        }
        if (tdl.getSequences().isEmpty() && simpleProcessors.isEmpty()) {
            String message = "No Sequences in TDL and There are no simple-processors to run. "
                    + "Either add a tdl-file or add a simple-processor.";
            throw new IllegalStateException(message);
        }

        // Log stuff here
        LOG.debug("Actual used TDL:\n" + tdl);
        LOG.debug("Timeout set to: " + timeoutSeconds + " seconds.");
        LOG.debug("tdlValidationEnabled: " + tdlValidationEnabled);
        LOG.debug("isPreprocessorUsed: " + isPreprocessorUsed);
        LOG.debug("Number of EventIterators: " + eventIterators.size());
        LOG.debug("Number of SimpleProcessors: " + simpleProcessors.size());

        stoppables.add(() -> executor.shutdownNow());
    }

    private void setApiConfigurationIfAvailableFromServiceLoader() {
        ServiceLoader<ApiConfigurationProvider> serviceLoader = ServiceLoader
                .load(ApiConfigurationProvider.class);
        serviceLoader.forEach(apiConf -> {
            if (metadataFactory == null) {
                EventMetaDataFactory mdFactory = apiConf.metaDataFactory();
                if (mdFactory != null) {
                    metadataFactory = mdFactory;
                }
            }
            if (valueFactory == null) {
                ValueFactory vFactory = apiConf.valueFactory();
                if (vFactory != null) {
                    valueFactory = vFactory;
                }
            }
        });
    }

    private void checkNotNull(Object o, Class<?> type) {
        if (o == null) {
            String message = String.format(
                    "No '%s' implementation found! Set one by calling method on %s",
                    type.getSimpleName(), EventProcessorBuilder.class.getSimpleName());
            throw new IllegalStateException(message);
        }
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

    StartedEventProcessor start() {
        CachingProcessor cachedEvents = util.startProcessingEventsAndCreateCache(
                pipeline, eventIterators, stoppables, simpleProcessors, executor);
        if (!tdl.getSequences().isEmpty()) {
            tdlProcessorResults = newFutureTask(new TdlProcessor(cachedEvents, this));
        } else {
            tdlProcessorResults = newInvalidCompletedFuture();
        }
        return this;
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
            public CompletedEventProcessor get(long timeout, TimeUnit unit)
                    throws InterruptedException,
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
        FutureTask<CompletedEventProcessor> task = new FutureTask<>(r);
        new Thread(task).start();
        return task;
    }

    /**
     * Waits until all Sequences have successfully ended. All EventProcessors
     * are automatically shutdown before this method returns.
     * <p>
     * If no Sequences are running, this method returns immediately (for example
     * if only the console logger is enabled) and EventProcessors are not
     * shutdown.
     *
     * @throws TdlFailureException
     *             when at least one EventProcessor (Sequence) fails or times
     *             out.
     */
    public void awaitSuccess() {
        CompletedEventProcessor completed = awaitCompletion();

        if (completed.isValid() || completed.getSequenceResults().isEmpty()) {
            return;
        }

        StringBuilder errorMessage = new StringBuilder();
        for (SequenceResult result : completed.getSequenceResults()) {
            if (!result.isValid()) {
                errorMessage.append(format(ERROR_MESSAGE, result.getName(), result.getCauses()));
            }
        }
        throw new TdlFailureException(errorMessage.toString());
    }

    /**
     * Waits until all Sequences have ended. All EventProcessors are
     * automatically shutdown before this method returns.
     * <p>
     * If no Sequences are running, this method returns immediately (for example
     * if only the console logger is enabled) and EventProcessors are not
     * shutdown.
     *
     * @throws TimeoutException
     *             when at least one EventProcessor (Sequence) fails, or a
     *             timeout occurs.
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
     * Returns true if EventProcessor has completed. Use
     * {@link #awaitCompletion()} to get the result.
     *
     * @return true if EventProcessor is completed, otherwise false.
     */

    public boolean isCompleted() {
        return tdlProcessorResults.isDone();
    }

}
