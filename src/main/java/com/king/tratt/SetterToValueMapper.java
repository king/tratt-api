// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static com.king.tratt.Tratt.values;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.tdl.CheckPoint;

class SetterToValueMapper {
    private ValueFactory valueFactory;

    SetterToValueMapper(ValueFactory valueFactory) {
        this.valueFactory = valueFactory;
    }

    Stream<Entry<String, Value>> getValues(CheckPoint cp) {
        return getValues(cp.getEventType(), cp.getSet());
    }

    private Stream<Entry<String, Value>> getValues(String eventName, List<String> set) {
        return VariableParser.parse(set).entrySet().stream()
                .map(entry -> {
                    Value value = getValue(eventName, entry.getValue());
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), value);
                });
    }

    Value getValue(String eventName, String strValue) {
        if (util.isLong(strValue) || util.isBoolean(strValue)) {
            return values.constant(strValue);
        }
        if (strValue.startsWith("'") && strValue.endsWith("'")) {
            // remove leading and trailing single quote (')
            return values.constant(strValue.replaceAll("^'|'$", ""));
        }
        return Optional
                .ofNullable(valueFactory.getValue(eventName, strValue))
                .orElseThrow(() -> {
                    String message = "Bad set value '%s'. Valid examples are: myVar=fieldName or myVar='string' or myVar=123";
                    return new IllegalStateException(String.format(message, strValue));
                });
    }
}
