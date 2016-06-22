// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static com.king.tratt.internal.Util.concat;
import static com.king.tratt.internal.Util.requireNonNull;
import static com.king.tratt.internal.Util.requireNonNullElements;
import static java.util.Arrays.asList;

/**
 * Tratt API entry point.
 */
public final class Tratt {
    public static final Util util = new Util();
    static final Values values = new Values();

    private Tratt() {
        throw new AssertionError("Not meant for instantiation!");
    }

    /**
     * Use this for configuring how event flow verification is done, using a TDL
     * file.
     *
     * @return {@link EventProcessorBuilder}
     */
    public static EventProcessorBuilder newEventProcessorBuilder() {
        return new EventProcessorBuilder();

    }

    /**
     * Use this to cache events to be used later. For example if you want to
     * cache events during installation of an app/game. Make sure to start it
     * before your app/game is installed.
     * <p>
     * Use the following method to use the cached events.
     * {@link EventProcessorBuilder#setPreprocessor(Preprocessor)}
     *
     * @return {@link PreprocessorBuilder}
     */
    public static PreprocessorBuilder newPreprocessor() {
        return new PreprocessorBuilder();
    }

    /**
     * Use this if you want to multicast the cached events from a
     * {@link Preprocessor} into multiple {@link EventProcessorBuilder}'s.
     *
     * @param preprocessor
     * @param first
     * @param rest
     * @return
     */
    public static Multicaster newMulticaster(Preprocessor preprocessor, EventProcessorBuilder first,
            EventProcessorBuilder... rest) {
        requireNonNull(first, "first");
        requireNonNull(rest, "rest");
        requireNonNullElements(asList(rest), "rest");
        return new Multicaster(preprocessor, concat(first, rest)).start();
    }
}
