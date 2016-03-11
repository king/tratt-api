package com.king.tratt.test.imp;

import static java.lang.ClassLoader.getSystemResourceAsStream;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.king.tratt.metadata.spi.EventMetaDataFactory;

public class TestEventMetaDataFactory implements EventMetaDataFactory<TestEventMetaData> {

    private static Map<String, JsonObject> eventTypes = new HashMap<>();

    public TestEventMetaDataFactory() {
        Reader reader = new InputStreamReader(getSystemResourceAsStream("com/king/tratt/test/imp/EventType.json"));
        JsonArray array = new Gson().fromJson(reader, JsonElement.class).getAsJsonArray();
        array.forEach(e -> {
            JsonObject o = (JsonObject) e;
            eventTypes.put(o.get("name").getAsString(), o);
        });
    }

    @Override
    public TestEventMetaData getEventMetaData(String eventName) {
        if (eventTypes.containsKey(eventName)) {
            return new TestEventMetaData(eventTypes.get(eventName));
        }
        return unrecognizedEventMetaData();
    }

}
