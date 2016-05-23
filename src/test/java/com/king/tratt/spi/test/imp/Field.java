// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt.spi.test.imp;

import com.google.gson.JsonObject;

public class Field {

    private JsonObject json;
    private int i;

    public Field(int i, JsonObject json) {
        this.i = i;
        this.json = json;
    }

    public int getIndex() {
        return i;
    }

    public String getName() {
        return json.get("name").getAsString();
    }

    public String getType() {
        return json.get("type").getAsString();
    }

    @Override
    public String toString() {
        return json.toString();
    }

}
