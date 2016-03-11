package com.king.tratt;

import static java.util.Arrays.asList;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.DebugStringAware;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.SufficientContextAware;
import com.king.tratt.spi.Value;

class _InternalUtil {

    private static final Pattern IS_BOOLEAN = Pattern.compile("true|false", CASE_INSENSITIVE);

    private _InternalUtil() { /* Not meant for instantiation */ }

    //    public static void main(String[] args) {
    //        Object[] arr = new Object[] {
    //                1,
    //                new Number[] { 1.1, 1.2 },
    //                Arrays.asList(2, 3, 4),
    //                new Object[] {
    //                        new Float[] { 5.1F, 5.2F, 5.3F },
    //                        5,
    //                        6,
    //                        7 },
    //                10 };
    //        System.out.println(TrattUtil.flatten(Arrays.asList(arr)));
    //    }

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

    //    static Object[] flatten(Object... objects) {
    //        return flatten2(new ArrayList<>(), objects).toArray();
    //    }
    //
    //    static <T> List<T> flatten(List<T> list, T... objects) {
    //        for (T o : objects) {
    //            Class<?> c = o.getClass();
    //            if (c.isArray()) {
    //                flatten(list, (T[]) o);
    //            } else if (Collection.class.isAssignableFrom(c)) {
    //                flatten(list, ((Collection<?>) o).toArray());
    //            } else {
    //                list.add(o);
    //            }
    //        }
    //        return list;
    //    }

    ////    static <T> T[] toArray(Class<T> type, List<? extends T> list) {
    ////        @SuppressWarnings("unchecked")
    ////        T[] arr = (T[]) Array.newInstance(type, list.size());
    ////        return list.toArray(arr);
    ////    }

    //    static <T> T[] flattenToArray(Class<T> type, Object... objects) {
    //        List<T> list = flatten(new ArrayList<T>(), objects);
    //        return toArray(type, list);
    //    }

    /*
     * static <T> List<T> flatten(Object... objects) {
     * return flatten(new ArrayList<>(), objects);
     * }
     * static <T> List<T> flatten(List<T> list, Object... objects) {
     * for (Object o : objects) {
     * Class<?> c = o.getClass();
     * if (c.isArray()) {
     * flatten(list, (Object[]) o);
     * } else if (Collection.class.isAssignableFrom(c)) {
     * flatten(list, ((Collection<?>) o).toArray());
     * } else {
     * @SuppressWarnings("unchecked")
     * T t = (T) o;
     * list.add(t);
     * }
     * }
     * return list;
     * }
     */
    //    static <E extends Event> String debugStringByGet(E e, Context context, String template, List<? extends Value<E>> awares) {
    //        return String.format(template, toObjectListByGet(e, context, awares).toArray());
    //    }

    //    static <E extends Event> List<Object> byGet(E e, Context context, List<Value<E>> values, Value<E> value) {
    //        return byGet(e, context, TrattUtil.flatten(asList(values, value)));
    //    }

    //    static <E extends Event> List<Object> byGet(E e, Context context, Value<E> value1, Value<E> value2) {
    //        return byGet(e, context, asList(value1, value2));
    //    }
    //
    //    static <E extends Event> List<Object> byGet(E e, Context context, Value<E> value1) {
    //        return byGet(e, context, asList(value1));
    //    }
    //
    //    static <E extends Event> List<Object> byGet(E e, Context context, List<Value<E>> values) {
    //        List<Object> result = new ArrayList<>();
    //        for (Value<E> v : values) {
    //            result.add(new Object() {
    //
    //                @Override
    //                public String toString() {
    //                    return v.asString(e, context);
    //                }
    //            });
    //        }
    //        return result;
    //    }

    //    static <E extends Event> String formatTemplate(String template, Object... args) {
    //        return format(template, TrattUtil.toArray(Object.class, TrattUtil.flatten(args)));
    //    }

    //    @SafeVarargs
    //    static <E extends Event> String debugString(E e, Context context, String template,
    //            DebugStringAware<E>... awares) {
    //        return debugString(e, context, template, Arrays.asList(awares));
    //    }
    //
    //    static <E extends Event> String debugString(E e, Context context, String template,
    //            List<? extends DebugStringAware<E>> awares) {
    //        return String.format(template, toDebugStringObjectList(e, context, awares).toArray());
    //    }

    //    static <E extends Event> List<Object> byDebugString(E e, Context context, DebugStringAware<E> value,
    //            List<? extends DebugStringAware<E>> values) {
    //        return byDebugString(e, context, flatten(asList(value, values)));
    //    }

    //    static <E extends Event> List<Object> byDebugString(E e, Context context, DebugStringAware<E> aware) {
    //        return byDebugString(e, context, asList(aware));
    //    }
    //
    //    static <E extends Event> List<Object> byDebugString(E e, Context context, DebugStringAware<E> aware1,
    //            DebugStringAware<E> aware2) {
    //        return byDebugString(e, context, asList(aware1, aware2));
    //    }
    //
    //    static <E extends Event> List<Object> byDebugString(E e, Context context,
    //            List<? extends DebugStringAware<E>> awares) {
    //        List<Object> result = new ArrayList<>();
    //        for (DebugStringAware<E> o : awares) {
    //            result.add(new Object() {
    //
    //                @Override
    //                public String toString() {
    //                    return o.toDebugString(e, context);
    //                }
    //            });
    //        }
    //        return result;
    //    }

    //    static <E extends Event> boolean hasSufficientContext(E e, Context context, SufficientContextAware<E> value,
    //            List<Value<E>> values) {
    //        return hasSufficientContext(e, context, concat(value, values));
    //    }

    static <E extends Event> boolean hasSufficientContext(E e, Context context,
            List<? extends SufficientContextAware<E>> awares) {
        for (SufficientContextAware<E> aware : awares) {
            if (!aware.hasSufficientContext(e, context)) {
                return false;
            }
        }
        return true;
    }


    //    static <E extends Event> boolean hasSufficientContext(E e, Context context, Value<E> value,
    //            List<Value<E>> values) {
    //        return hasSufficientContext(e, context, concat(value, values));
    //    }
    //
    //    @SafeVarargs
    //    static <E extends Event> boolean hasSufficientContext(E e, Context context,
    //            SufficientContextAware<E>... awares) {
    //        for (SufficientContextAware<E> aware : awares) {
    //            if (!aware.hasSufficientContext(e, context)) {
    //                return false;
    //            }
    //        }
    //        return true;
    //    }
    //
    //    static <E extends Event> Value<E>[] concat(Value<E> aware, Value<E>[] awares) {
    //        return concat(aware, Arrays.asList(awares));
    //    }

    //    @SafeVarargs
    //    static <T> T[] concat(T value, T... values) {
    //        List<T> list = new ArrayList<>();
    //        list.add(value);
    //        list.addAll(Arrays.asList(values));
    //        return toArray(list);
    //        //
    //        //        T[] all = (T[]) Array.newInstance(value.getClass(), values.length + 1);
    //        //        all[0] = value;
    //        //        System.arraycopy(values, 0, all, 1, values.length);
    //        //        return all;
    //    }

    static <T> List<T> concat(T value, List<T> values) {
        List<T> list = new ArrayList<>();
        list.add(value);
        list.addAll(values);
        return list;
    }

    //    @SafeVarargs
    //    static <T> List<T> concat(T value, T... values) {
    //        return concat(value, Arrays.asList(values));
    //    }
    //
    //    static <T> List<T> concat(T value, List<? extends T> values) {
    //        List<T> list = new ArrayList<>();
    //        list.add(value);
    //        list.addAll(values);
    //        return list;
    //    }
    //
    //    static <T> List<T> concat(List<? extends T> values, T value) {
    //        List<T> list = new ArrayList<>();
    //        list.addAll(values);
    //        list.add(value);
    //        return list;
    //    }

    //    @SuppressWarnings("unchecked")
    //    static <T> T[] toArray(List<T> list) {
    //        System.out.println(list.getClass().getComponentType());
    //        T[] all = (T[]) Array.newInstance(list.getClass().getComponentType(), list.size());
    //        return list.toArray(all);
    //    }

    //    public static <E extends Event> String formatTemplate(String template, Object arg1, Object arg2) {
    //        return formatTemplate(template, Arrays.asList(arg1, arg2));
    //    }
    //
    //    public static <E extends Event> String formatTemplate(String template, Object arg1) {
    //        return formatTemplate(template, Arrays.asList(arg1));
    //    }
    //
    //    public static <E extends Event> String formatTemplate(String template, List<Object> args) {
    //        Object[] flattenArgs = flatten(args).toArray();
    //        try {
    //            return format(template, flattenArgs);
    //        } catch (Exception e) {
    //            // can't use String.format(...) here, since 'template' could be malformed.
    //            String message = "Illegal format string '" + template + "'. " + e.getMessage() + ". "
    //                    + " args[" + flattenArgs.length + "]: " + Arrays.toString(flattenArgs);
    //            throw new IllegalStateException(message, e);
    //        }
    //    }

    //    return formatTemplate(e, context, "(#d * #d)#g", left, right, this);


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
    public static <E extends Event> String format(final E e, final Context context, String format, Object... args) {
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
