// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt.tdl;

import static com.king.tratt.internal.Util.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.king.tratt.tdl.TdlInternal.CheckPointInternal;

public final class CheckPointBuilder {
    final String eventType;
    String match = "";
    String validate = "";
    List<String> set = new ArrayList<>();

    /**
     * Retrieve a new {@link CheckPointBuilder} for the given EventType. Use the
     * {@link CheckPointBuilder} to create a new {@link CheckPoint}.
     *
     * @param eventType
     * @return
     */
    public static CheckPointBuilder forEvent(String eventType) {
        return new CheckPointBuilder(eventType);
    }

    /**
     * Retrieve a new {@link CheckPointBuilder} based on the given
     * {@link CheckPoint}, i.e. copy its values and set them as default. Use
     * this {@link CheckPointBuilder} to modify any of those values.
     *
     * @param checkPoint
     *            the {@link CheckPoint} used as default value.
     * @return {@link CheckPointBuilder}
     */
    public static CheckPointBuilder copyOf(CheckPoint checkPoint) {
        return new CheckPointBuilder(checkPoint);
    }

    CheckPointBuilder(String eventType) {
        this.eventType = eventType;
    }

    CheckPointBuilder(CheckPoint checkPoint) {
        eventType = checkPoint.getEventType();
        match = checkPoint.getMatch();
        validate = checkPoint.getValidate();
        set = new ArrayList<>(checkPoint.getSet());
    }

    public CheckPoint build() {
        CheckPointInternal cp = new CheckPointInternal();
        cp.eventType = eventType;
        cp.match = match;
        cp.set = new ArrayList<>(set);
        cp.validate = validate;
        return new CheckPoint(cp);
    }

    public CheckPointBuilder match(String match) {
        requireNonNull(match, "match");
        this.match = match;
        return this;
    }

    public CheckPointBuilder set(String... nameValuePairs) {
        for (String nameValuePair : nameValuePairs) {
            if (nameValuePair == null || nameValuePair.isEmpty() || !nameValuePair.contains("=")) {
                String message = "VarArg argument contains illegal value: %s";
                throw new NullPointerException(
                        String.format(message, Arrays.toString(nameValuePairs)));
            }
        }
        set.addAll(Arrays.asList(nameValuePairs));
        return this;
    }

    public CheckPointBuilder validate(String validate) {
        requireNonNull(validate, "validate");
        this.validate = validate;
        return this;
    }

}
