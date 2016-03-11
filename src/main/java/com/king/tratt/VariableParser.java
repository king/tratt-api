package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class VariableParser {

    private VariableParser() {
        throw new AssertionError("Not meant for instantiation!");
    }

    static Map<String, String> parse(List<String> nameValues) {
        return parse("", nameValues);
    }

    static Map<String, String> parse(String keyPrefix, List<String> nameValues) {
        if (keyPrefix == null) {
            throw util.nullArgumentError("keyPrefix");
        }
        if (nameValues == null) {
            throw util.nullArgumentError("nameValues");
        }
        if (nameValues.isEmpty()) {
            return Collections.emptyMap();
        }
        return nameValues.stream()
                .map(s -> parse(keyPrefix, s))
                .collect(toMap(k -> k.name, v -> v.value));
    }

    static NameValue parse(String nameValue) {
        return parse("", nameValue);
    }

    static NameValue parse(String keyPrefix, String nameValue) {
        if (nameValue == null) {
            String message = "Name/Value cannot be null: '%s'";
            throw new NullPointerException(String.format(message, nameValue));
        }
        if (nameValue.isEmpty()) {
            String message = "Name/Value cannot be an empty string: '%s'";
            throw new IllegalArgumentException(format(message, nameValue));
        }
        if (!nameValue.contains("=")) {
            String message = "Name/Value must be delimit by a '=' sign: '%s'";
            throw new IllegalArgumentException(String.format(message, nameValue));
        }
        String[] split = nameValue.split("=", 2);
        String name = split[0].trim();
        if (name.isEmpty()) {
            String message = "Name part cannot be empty string: '%s'";
            throw new IllegalArgumentException(String.format(message, nameValue));
        }
        String value = split[1].trim();
        return new NameValue(keyPrefix + name, value);
    }

    static class NameValue {
        final String name;
        final String value;

        NameValue(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
