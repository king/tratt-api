package com.king.tratt.spi;

import static java.lang.String.format;

import java.util.List;
import java.util.Objects;

final class Util {

    Util() {
        throw new AssertionError("Not meant for instantiation!");
    }

    static <T> T requireNonNull(T t, String argName) {
        if (t == null) {
            String message = "'%s' must not be null!";
            throw new NullPointerException(format(message, argName));
        }
        return t;
    }

    static void requireNonNullElements(List<?> list, String string) {
        if (list.stream().anyMatch(Objects::isNull)) {
            String message = "'%s' must not contain any null objects! %s";
            throw new NullPointerException(format(message, string, list));
        }
    }
}
