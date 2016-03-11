package com.king.tratt;

import com.king.tratt.metadata.spi.Event;

/**
 * Tratt API entry point.
 */
public final class Tratt {
    public static final Util util = new Util();
    public static final Values values = new Values();

    private Tratt() {
        throw new AssertionError("Not meant for instantiation!");
    }

    /**
     * Use this for configuring how event flow verification is done, using a TDL file.
     *
     * @return {@link EventProcessorBuilder}
     */
    public static <E extends Event> EventProcessorBuilder<E> newEventProcessorBuilder() {
        return new EventProcessorBuilder<E>();

    }

    /**
     * Use this for configuring how event flow verification is done, using a
     * simplified API, where you don't need to know about TDL files (as it will be generated
     * for you).
     *
     * @return {@link SimpleEventProcessorBuilder}
     */
    //    public static SimpleEventProcessorBuilder newSimpleEventProcessorBuilder() {
    //        return new SimpleEventProcessorBuilder();
    //    }

    /**
     * Use this to cache events to be used later. For example if you want to cache events
     * during installation of an app/game. Make sure to start it before your app/game is
     * installed.
     * <p>
     * Use the following method to use the cached events.
     * {@link EventProcessorBuilder#setPreprocessor(Preprocessor)}
     *
     * @return {@link PreprocessorBuilder}
     */
    //    public static PreprocessorBuilder newPreprocessorBuilder() {
    //        return new PreprocessorBuilder();
    //    }

}
