package com.king.tratt.metadata.test.imp;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.king.tratt.metadata.spi.EventMetaData;

public class TestEventMetaData implements EventMetaData {

    private JsonObject jsonObject;
    private Map<String, Field> fields = new HashMap<>();

    public TestEventMetaData(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        JsonArray array = jsonObject.get("parameters").getAsJsonArray();
        int i = 0;
        for (JsonElement e : array) {
            JsonObject o = (JsonObject) e;
            fields.put(o.get("name").getAsString(), new Field(i, o));
            i++;
        }

    }

    Field getField(String fieldName) {
        return fields.get(fieldName);
    }

    @Override
    public long getId() {
        return Long.parseLong(jsonObject.get("id").getAsString());
    }

    @Override
    public String getName() {
        return jsonObject.get("name").getAsString();
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

}
