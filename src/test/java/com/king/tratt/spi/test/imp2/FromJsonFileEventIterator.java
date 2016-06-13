package com.king.tratt.spi.test.imp2;

import static java.lang.ClassLoader.getSystemResourceAsStream;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.EventIterator;

public class FromJsonFileEventIterator implements EventIterator {
    private final String path;
    private Iterator<JsonElement> iterator;
    private AtomicBoolean running = new AtomicBoolean();

    public FromJsonFileEventIterator(String Path) {
        this.path = Path;
    }

    @Override
    public boolean hasNext() {
        return running.get() && iterator.hasNext();
    }

    @Override
    public Event next() {
        return new JsonEvent(iterator.next());
    }

    @Override
    public void start() {
        running.set(true);
        Reader reader = new InputStreamReader(getSystemResourceAsStream(path));
        JsonParser jsonParser = new JsonParser();
        JsonElement arr = jsonParser.parse(reader);
        JsonArray a = arr.getAsJsonArray();
        iterator = a.iterator();
    }

    @Override
    public void stop() {
        running.set(false);
    }

}
