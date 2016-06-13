package com.king.tratt.spi.test.imp2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.king.tratt.spi.Event;

public class JsonEvent implements Event {
    private final JsonObject jsonObject;

    public JsonEvent(JsonElement jsonElement) {
        this.jsonObject = jsonElement.getAsJsonObject();
    }

    @Override
    public String getId() {
        return get("type");
    }

    @Override
    public long getTimestampMillis() {
        return Long.valueOf(get("logTime"));
    }

    public String get(String fieldName) {
        JsonElement jsonElem = null;
        JsonObject jsonObj = jsonObject;
        for (String subPath : fieldName.split("\\.")) {
            jsonElem = jsonObj.get(subPath);
            if (jsonElem == null) {
                return String.format("[@ERROR incorrect event field name: '%s']", fieldName);
            }
            if (jsonElem.isJsonObject()) {
                jsonObj = jsonElem.getAsJsonObject();
            }
        }

        if (jsonElem.isJsonObject()) {
            return jsonElem.toString();
        }
        return jsonElem.getAsString();
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

}
