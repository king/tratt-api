package com.king.tratt;

import static com.king.tratt.Tratt.util;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.king.tratt.tdl.CheckPoint;

class LocalVariablesToValueMapper<E extends Event> {

    private StartedEventProcessor<E> started;

    public LocalVariablesToValueMapper(StartedEventProcessor<E> started) {
        this.started = started;
    }

    Stream<Entry<String, Value<E>>> getValues(CheckPoint checkPoint) {
        EventMetaData eventMetaData = started.metadataFactory.getEventMetaData(checkPoint.getEventType());
        List<String> set = checkPoint.getSet();
        return VariableParser.parse(set).entrySet().stream().map(entry -> {
            Value<E> value = Optional
                    .ofNullable(started.valueFactory.getValue(eventMetaData.getName(), entry.getValue()))
                    .orElseGet(() -> tryGetConstantValue(entry.getValue()));
            return new AbstractMap.SimpleEntry<String, Value<E>>(entry.getKey(), value);
        });
    }

    private Value<E> tryGetConstantValue(final String str) {
        String value = null;
        if (util.isLong(str) || util.isBoolean(str)) {
            value = str;
        } else if (str.length() < 2) {
            // do nothing
        } else if (str.startsWith("'") && str.endsWith("'")) {
            // remove leading and trailing single quote (')
            value = str.replaceAll("^'|'$", "");
        }
        if (value == null) {
            String message = "Bad set expression '%s'. Valid examples are: myVar=fieldName or myVar='constant123'";
            throw new IllegalStateException(String.format(message, str));
        }
        return Values.constant(value);
    }

}
