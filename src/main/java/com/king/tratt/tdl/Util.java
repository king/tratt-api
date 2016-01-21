package com.king.tratt.tdl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class Util {

    private static final Gson PRETTY_WRITER = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Gson WRITER = new Gson();

    private Util() {
        throw new AssertionError("Not meant for instantiation");
    }

    static <T> List<T> concat(T first, @SuppressWarnings("unchecked") T... rest) {
        List<T> list = new ArrayList<T>();
        list.add(first);
        list.addAll(Arrays.asList(rest));
        return list;
    }

    static String asJson(Object o) {
        return WRITER.toJson(o);
    }

    static String asJsonPrettyPrinted(Object o) {
        return PRETTY_WRITER.toJson(o);
    }

    static NullPointerException nullArgumentError(String name) {
        String message = "Argument '%s' is null.";
        return new NullPointerException(format(message, name));
    }

    static IllegalArgumentException varArgError(String... args) {
        return varArgError((Object[]) args);
    }

    static IllegalArgumentException varArgError(Object... args) {
        return varArgError(Arrays.asList(args));
    }

    static IllegalArgumentException varArgError(Collection<?> args) {
        String message = "One of the args is either null or empty-string: %s";
        return new IllegalArgumentException(format(message, args.toString()));
    }

    static IllegalArgumentException emptyStringArgumentError(String name) {
        String message = "Argument '%s' is empty string.";
        return new IllegalArgumentException(format(message, name));
    }

    static <T> Set<T> concatAsSet(T first, T[] rest) {
        return new LinkedHashSet<T>(concat(first, rest));
    }

    static boolean containsEitherNullOrEmptyStringElements(Collection<?> c) {
        return c.contains(null) || c.contains("");
    }
}
