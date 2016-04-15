/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.internal;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * These utilities methods are only meant for internal usage. Method signatures
 * can change at any time, without any notice.
 *
 */
public final class Util {

    private Util() {
        throw new AssertionError("Not meant for instantiation");
    }

    public static <T> List<T> concat(T first, @SuppressWarnings("unchecked") T... rest) {
        List<T> list = new ArrayList<T>();
        list.add(first);
        list.addAll(asList(rest));
        return list;
    }

    public static <T> T requireNonNull(T t, String argName) {
        if (t == null) {
            String message = "'%s' must not be null!";
            throw new NullPointerException(format(message, argName));
        }
        return t;
    }

    public static void requireNonNullElements(List<?> list, String argName) {
        if (list.stream().anyMatch(Objects::isNull)) {
            String message = "'%s' must not contain any null elements! %s";
            throw new NullPointerException(format(message, argName, list));
        }
    }

    public static void requireNonEmptyString(String str, String argName) {
        if (str.trim().isEmpty()) {
            String message = "'%s' must not be empty string!";
            throw new IllegalArgumentException(format(message, argName));
        }
    }

    public static void requireNoneNegative(long duration, String argName) {
        if (duration < 0) {
            String message = "'%s' must not be negative!";
            throw new IllegalArgumentException(message);
        }
    }

}
