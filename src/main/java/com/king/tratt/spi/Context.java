// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt.spi;

import java.util.Map.Entry;
import java.util.Set;

public interface Context {

    boolean containsKey(String name);

    Object get(String name);

    /**
     *
     * @param key
     * @param value
     *            can only be of type: long, String boolean
     */
    void set(String key, Object value);

    Set<Entry<String, Object>> entrySet();
}
