package com.king.tratt.metadata.test.imp;

import static com.king.tratt.Tratt.util;
import static java.nio.file.Files.readAllLines;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.king.tratt.EventIterator;

public class TestFromFileEventIterator implements EventIterator<TestEvent> {
    private volatile Iterator<String> iterator;
    private AtomicBoolean hasNext = new AtomicBoolean(true);
    final Path path;

    public TestFromFileEventIterator(String path) {
        this.path = util.toPath(path);
    }

    @Override
    public boolean hasNext() {
        return hasNext.get() && iterator.hasNext();
    }

    @Override
    public TestEvent next() {
        String eventString = iterator.next();
        String[] parameters = eventString.split("\\s+");
        System.out.println(Arrays.toString(parameters));
        TestEvent e = new TestEvent(parameters[0], parameters[1], Arrays.copyOfRange(parameters, 2, parameters.length));
        return e;
    }

    @Override
    public void start() {
        hasNext.set(true);
        try {
            iterator = readAllLines(path, Charset.forName("UTF-8")).iterator();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        hasNext.set(false);
    }

}
