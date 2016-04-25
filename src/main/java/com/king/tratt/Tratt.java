/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.internal.Util.concat;
import static com.king.tratt.internal.Util.requireNonNull;
import static com.king.tratt.internal.Util.requireNonNullElements;
import static java.util.Arrays.asList;

/**
 * Tratt API entry point.
 */
public final class Tratt {
    static final InternalUtil util = new InternalUtil();
    static final Values values = new Values();

    private Tratt() {
        throw new AssertionError("Not meant for instantiation!");
    }

    /**
     * Use this for configuring how event flow verification is done, using a TDL file.
     *
     * @return {@link EventProcessorBuilder}
     */
    public static EventProcessorBuilder newEventProcessorBuilder() {
        return new EventProcessorBuilder();

    }

    public static PreprocessorBuilder newPreprocessor() {
        return new PreprocessorBuilder();
    }

    public static Multicaster newMulticaster(Preprocessor pre, EventProcessorBuilder first,
            EventProcessorBuilder... rest) {
        requireNonNull(first, "first");
        requireNonNull(rest, "rest");
        requireNonNullElements(asList(rest), "rest");
        return new Multicaster(pre, concat(first, rest)).start();
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
    // TODO!
    //    public static PreprocessorBuilder newPreprocessorBuilder() {
    //        return new PreprocessorBuilder();
    //    }

}
