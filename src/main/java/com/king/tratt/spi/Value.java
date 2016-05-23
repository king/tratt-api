// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt.spi;

import static com.king.tratt.internal.Util.requireNonNull;
import static com.king.tratt.internal.Util.requireNonNullElements;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import com.king.tratt.tdl.CheckPoint;

/**
 * Instances of this class represents a value in a {@link CheckPoint}'s
 * {@code match}, or {@code validate} fields.
 *
 */
public abstract class Value implements DebugStringAware, SufficientContextAware {
    private final List<? extends SufficientContextAware> awares;

    static List<Class<?>> supportedReturnTypes() {
        return asList(Boolean.class, Long.class, String.class, Object.class);
    }

    @SafeVarargs
    public Value(SufficientContextAware... awares) {
        this(asList(requireNonNull(awares, "awares")));
    }

    public Value(List<? extends SufficientContextAware> awares) {
        requireNonNull(awares, "awares");
        requireNonNullElements(awares, "awares");
        checkReturnType();
        this.awares = awares;
    }

    private void checkReturnType() {
        List<Method> methodWithUnsupportedReturnTypes = getAllMethods(getClass())
                .filter(m -> m.getName().equals("getImp"))
                .filter(m -> m.getParameterTypes().length == 2)
                .filter(m -> Context.class.isAssignableFrom(m.getParameterTypes()[1]))
                .filter(m -> Event.class.isAssignableFrom(m.getParameterTypes()[0]))
                .filter(m -> !supportedReturnTypes().contains(m.getReturnType()))
                .collect(toList());
        if (!methodWithUnsupportedReturnTypes.isEmpty()) {
            String message = "Method 'getImp' has unsupported return type. Supported are: %s\n%s";
            message = String.format(message, supportedReturnTypes(),
                    methodWithUnsupportedReturnTypes);
            throw new UnsupportedReturnTypeException(message);
        }
    }

    /*
     * Recursively walks up the super classes hierarchy to find all declared
     * methods.
     */
    private Stream<Method> getAllMethods(Class<?> cls) {
        if (cls.equals(Value.class)) {
            return empty();
        }
        return concat(stream(cls.getDeclaredMethods()), getAllMethods(cls.getSuperclass()));
    }

    protected abstract Object getImp(Event e, Context context);

    public final Object get(Event event, Context context) {
        try {
            return getImp(event, context);
        } catch (Throwable t) {
            String message = "Unexpected crash! See underlying exceptions for more info.\n"
                    + "  value: %s; event: %s; context: %s";
            message = String.format(message, this, event, context);
            throw new IllegalStateException(message, t);
        }
    }

    @Override
    public boolean hasSufficientContext(Context context) {
        for (SufficientContextAware aware : awares) {
            if (!aware.hasSufficientContext(context)) {
                return false;
            }
        }
        return true;
    };

    final public String asString(Event e, Context context) {
        return String.valueOf(get(e, context));
    }
}
