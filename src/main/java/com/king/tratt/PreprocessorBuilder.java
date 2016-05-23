// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static com.king.tratt.internal.Util.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;
import com.king.tratt.spi.Stoppable;

public final class PreprocessorBuilder {

    final List<Stoppable> stoppables = new ArrayList<>();
    final BlockingQueue<Event> pipeline = new LinkedBlockingQueue<>();
    final List<EventIterator> eventIterators = new ArrayList<>();
    final List<SimpleProcessor> simpleProcessors = new ArrayList<>();

    PreprocessorBuilder() {
        /* for package private usage only */
    }

    public PreprocessorBuilder addEventIterator(EventIterator eventIterator) {
        requireNonNull(eventIterator, "eventIterator");
        eventIterators.add(eventIterator);
        return this;
    }

    public PreprocessorBuilder addSimpleProcessor(SimpleProcessor simpleProcessor) {
        requireNonNull(simpleProcessor, "simpleProcessor");
        simpleProcessors.add(simpleProcessor);
        return this;
    }

    // TODO add enableConsoleLogger(EventFilter...) method

    public Preprocessor start() {
        return new Preprocessor(this).start();
    }

}
