package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static com.king.tratt.Tratt.values;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.king.tratt.spi.Context;
import com.king.tratt.spi.DynamicValue;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

class FunctionFactoryJsonField<E extends Event> implements FunctionFactory<E> {
    Value<E> pathValue;
    Value<E> jsonValue;
    JsonParser jsonParser;

    @Override
    public String getName() {
        return "jsonfield";
    }

    @Override
    public int getNumberOfArguments() {
        return 2;
    }

    @Override
    public Value<E> create(List<Value<E>> arguments) {
        pathValue = arguments.get(0);
        jsonValue = arguments.get(1);
        jsonParser = new JsonParser();

        return new DynamicValue<E>() {

            @Override
            public String toDebugString(E e, Context context) {
                return util.format(e, context, "[[source:jsonfield('~g', '~g')]]~p", pathValue, jsonValue, this);
            }

            @Override
            protected Value<E> _get(E e, Context context) {
                String path = pathValue.asString(e, context);
                String json = jsonValue.asString(e, context);


                String result = null;
                try {
                    result = getJsonFieldValue(path, json);
                } catch (Throwable t) {
                    result = "[@ERROR malformed json string]";
                }
                return values.plain(result);
            }

            private String getJsonFieldValue(String path, String json) {
                JsonElement jsonElem = jsonParser.parse(json);
                for (String subPath : path.split("\\.")) {
                    JsonObject jsonObj = jsonElem.getAsJsonObject();
                    jsonElem = jsonObj.get(subPath);
                    if (jsonElem == null) {
                        return String.format("[@ERROR incorrect json path: '%s']", path);
                    }
                }
                if (jsonElem.isJsonObject()) {
                    return jsonElem.toString();
                }
                return jsonElem.getAsString();

            }
        };
    }
}