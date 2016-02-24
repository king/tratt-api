package com.king.tratt.test.imp;

import static java.nio.file.Files.readAllLines;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.king.tratt.TrattUtil;
import com.king.tratt.spi.EventIterator;

public class TestFileEventIterator implements EventIterator<TestEvent> {
    private volatile Iterator<String> iterator;
    private AtomicBoolean hasNext = new AtomicBoolean(true);
    final Path path;

    public TestFileEventIterator(String path) {
        this.path = TrattUtil.toPath(path);
    }

    @Override
    public boolean hasNext() {
        return hasNext.get() && iterator.hasNext();
    }

    @Override
    public TestEvent next() {
        String eventString = iterator.next();
        String[] parameters = eventString.split("\\s+");
        TestEvent e = new TestEvent(parameters[0], parameters[1], Arrays.copyOfRange(parameters, 3, parameters.length));
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
