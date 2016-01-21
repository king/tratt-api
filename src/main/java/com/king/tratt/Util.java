package com.king.tratt;

import static java.util.Arrays.asList;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.DebugStringAware;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.SufficientContextAware;
import com.king.tratt.spi.Value;

class Util {

    private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", CASE_INSENSITIVE);

    private Util() {
        throw new AssertionError("Not meant for instantiation");
    }


    /*
     * Check if a String can be parsed to a 'Long'.
     */
    static boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /*
     * Check if a String can be parsed to a 'Boolean'.
     */
    static boolean isBoolean(String nodeValue) {
        return IS_BOOLEAN.matcher(nodeValue).matches();
    }

    static <E extends Event> boolean hasSufficientContext(E e, Context context,
            List<? extends SufficientContextAware<E>> awares) {
        for (SufficientContextAware<E> aware : awares) {
            if (!aware.hasSufficientContext(e, context)) {
                return false;
            }
        }
        return true;
    }

    static <T> List<T> concat(T value, List<T> values) {
        List<T> list = new ArrayList<>();
        list.add(value);
        list.addAll(values);
        return list;
    }

    static NullPointerException nullArgumentError(String name) {
        String message = "Argument '%s' is null.";
        return new NullPointerException(String.format(message, name));
    }

    static IllegalArgumentException varArgError(String... args) {
        return varArgError((Object[]) args);
    }

    static IllegalArgumentException varArgError(Object... args) {
        String message = "One of the var-args is either null or empty: %s";
        String arraysToString = Arrays.toString(args);
        return new IllegalArgumentException(String.format(message, arraysToString));
    }

    static IllegalArgumentException emptyStringArgumentError(String name) {
        String message = "Argument '%s' is empty string.";
        return new IllegalArgumentException(String.format(message, name));
    }

    /*
     * Takes a protocol prefixed path. Accepted protocols are "file" and "classpath".
     * Example:
     * "classpath:root-folder/file.txt"
     * "file:/root/file.txt"
     * "file:/c:/temp/file.txt"
     * No prefix works as well, and will be used as: new File("path").
     */
    //    static Path toPath(String prefixedPath) {
    //        try {
    //            URI uri;
    //            if (prefixedPath.startsWith(CLASSPATH_PROTOCOL)) {
    //                String stringPath = prefixedPath.substring(CLASSPATH_PROTOCOL.length());
    //                uri = getSystemResource(stringPath).toURI();
    //            } else if (prefixedPath.startsWith(FILE_PROTOCOL)) {
    //                uri = new URL(prefixedPath).toURI();
    //            } else {
    //                uri = new File(prefixedPath).toURI();
    //            }
    //            return Paths.get(uri);
    //        } catch (Exception e) {
    //            throw new IllegalArgumentException(prefixedPath, e);
    //        }
    //
    //    }

    //    static <T> List<T> concat(T first, @SuppressWarnings("unchecked") T... rest) {
    //        List<T> list = new ArrayList<T>();
    //        list.add(first);
    //        list.addAll(Arrays.asList(rest));
    //        return list;
    //    }
    //
    static <E extends Event> String formatJoin(E e, Context context, String glue, String format,
            List<? extends Object> args) {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (Object o : args) {
            sb.append(separator);
            separator = glue;
            sb.append(format(e, context, format, o));
        }
        return sb.toString();
    }

    /**
     * The 'format' string can contain the following conversion patterns, where each conversion
     * pattern corresponds to an object in the args list (in order. e.g first conversion pattern
     * found * in 'format' corresponds to first element in args and so on).
     * '~d' (d as in debug):
     * '~g' (g as in get):
     * '~s' (s as in string):
     * '~p' (p as in plain):
     * check the below switch case to see what the conversion patterns do.
     *
     * @return a formatted string
     */
    private static final Pattern CONVERSION_PATTERN = Pattern.compile("(~[gdsp])");

    static <E extends Event> String format(final E e, final Context context, String format, Object... args) {
        List<Object> replacements = new ArrayList<>(asList(args));
        Matcher m = CONVERSION_PATTERN.matcher(format);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String conversion = m.group();
            Object arg = null;
            try {
                arg = replacements.remove(0);
                String replacement = doConversion(e, context, conversion, arg);
                m.appendReplacement(sb, quoteReplacement(replacement));
            } catch (Exception ex) {
                String message = "Cannot format '%s' with args '%s' due to underlying exception. conversion: %s, arg: %s ";
                throw new IllegalStateException(String.format(message, format, asList(args), conversion, arg), ex);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event> String doConversion(E e, Context context, String action, Object o) {
        switch (action) {
        case "~g":
            return ((Value<E>) o).asString(e, context);
        case "~d":
            return ((DebugStringAware<E>) o).toDebugString(e, context);
        case "~s":
            return o.toString();
        case "~p":
            return Values.plain(((Value<E>) o).get(e, context)).toDebugString(e, context);
        default:
            String message = "Unsupported conversion: '%s'";
            throw new IllegalStateException(String.format(message, action));
        }
    }
}
