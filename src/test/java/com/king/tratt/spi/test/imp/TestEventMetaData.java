/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.spi.test.imp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.king.tratt.spi.EventMetaData;

import java.util.HashMap;
import java.util.Map;

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
    public String getId() {
        return jsonObject.get("id").getAsString();
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
