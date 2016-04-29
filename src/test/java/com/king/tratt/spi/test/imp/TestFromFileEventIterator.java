/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi.test.imp;

import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.readAllLines;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.king.tratt.spi.EventIterator;

public class TestFromFileEventIterator implements EventIterator {
    private static final String CLASSPATH_PROTOCOL = "classpath:";
    private static final String FILE_PROTOCOL = "file:";
    private volatile Iterator<String> iterator;
    private AtomicBoolean hasNext = new AtomicBoolean(true);
    final Path path;

    public TestFromFileEventIterator(String path) {
        this.path = toPath(path);
    }

    @Override
    public boolean hasNext() {
        return hasNext.get() && iterator.hasNext();
    }

    @Override
    public TestEvent next() {
        String eventString = iterator.next();
        String[] parameters = eventString.split("\\s+");
        TestEvent e = new TestEvent(parameters[0], parameters[1],
                Arrays.copyOfRange(parameters, 2, parameters.length));
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

    Path toPath(String prefixedPath) {
        try {
            URI uri;
            if (prefixedPath.startsWith(CLASSPATH_PROTOCOL)) {
                String stringPath = prefixedPath.substring(CLASSPATH_PROTOCOL.length());
                uri = getSystemResource(stringPath).toURI();
            } else if (prefixedPath.startsWith(FILE_PROTOCOL)) {
                uri = new URL(prefixedPath).toURI();
            } else {
                uri = new File(prefixedPath).toURI();
            }
            return Paths.get(uri);
        } catch (Exception e) {
            throw new IllegalArgumentException(prefixedPath, e);
        }

    }

}
