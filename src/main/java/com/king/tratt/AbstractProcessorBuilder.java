package com.king.tratt;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.king.tratt.spi._ApiConfigurator;

abstract class AbstractProcessorBuilder<R, E extends Event> {

    //    protected final Map<Object, Processor<Event>> processors = new HashMap<>();
    protected final List<Stoppable> stoppables = new ArrayList<>();
    protected final Set<EventIterator> iterators = new HashSet<>();
    //    protected final StatisticsDataHolder statisticsDataHolder = new StatisticsDataHolder();
    protected BlockingQueue<E> pipeline = new LinkedBlockingQueue<>();
    protected PipelineProducerStrategy<E> producerStrategy = PipelineProducerStrategy.getDefault();
    //    final EventMetaDataProvider<FieldMetaData, EventMetaData<FieldMetaData>> metadataProvider;
    protected _ApiConfigurator<?> confProvider;

    protected AbstractProcessorBuilder() {
        ServiceLoader<_ApiConfigurator<?>> serviceLoader;
        // TODO setup ServiceLoader to find ApiConfiguratorProvider
        //        metadataProvider = new FromClassEventMetaDataProvider();
    }


    /**
     * Used for testing/debugging only.
     * <p>
     * Don't use unless you know what you are doing. No guarantees this method will stay backward
     * compatible (may change without any notice). No support is given.
     */
    //    @Deprecated
    //    public R usePrerecordedEventFile(String prefixedPath, boolean timestampAware) {
    //        addEventIterator(forPrerecordedFile(prefixedPath, timestampAware));
    //        statisticsDataHolder.kafkaTopics.add("usage of pre-recorded events.");
    //        return thisInstance();
    //    }

    /*
     * For debugging only.
     */
    //    public R enableRawEventFileLogger(String path) {
    //        processors.put("file-logger:" + path, fileLogger(path));
    //        return thisInstance();
    //    }

    /**
     * Logs all events to the console-log. Provide filters to filter out particular events.
     * Multiple filters can be added (with an AND relation).
     * <p>
     * NOTE! Filtering the console log will NOT affect the actual events that are considered for
     * verification. In other words, the filters only affects the content shown in the console log,
     * Nothing else.
     *
     * @param eventFilters Filter out events from console-log (only).
     * @return this builder.
     */
    //    public R enableConsoleLogger(EventFilter... eventFilters) {
    //        if (eventFilters == null) {
    //            throw nullArgumentError("eventFilters");
    //        }
    //        for (EventFilter f : eventFilters) {
    //            if (f == null) {
    //                throw varArgError((Object[]) eventFilters);
    //            }
    //        }
    //        processors.put("console-logger", consoleLogger(metadataProvider, eventFilters));
    //        return thisInstance();
    //    }

    @SuppressWarnings("unchecked")
    /*
     * This is used to avoid the @SuppressWarnings on every method.
     */
    R thisInstance() {
        return (R) this;
    }

}
