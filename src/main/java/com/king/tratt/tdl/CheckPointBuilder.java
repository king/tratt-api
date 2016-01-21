package com.king.tratt.tdl;

import static com.king.tratt.tdl.Util.nullArgumentError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.king.tratt.tdl.TdlInternal.CheckPointInternal;

public final class CheckPointBuilder {

    final String eventType;
    String match = "";
    String validate = "";
    List<String> set = new ArrayList<>();
    boolean optional = false;
    String label = "";

    /**
     * Retrieve a new {@link CheckPointBuilder} for the given EventType.
     * Use the {@link CheckPointBuilder} to create a new {@link CheckPoint}.
     *
     * @param eventType
     * @return
     */
    public static CheckPointBuilder forEvent(String eventType) {
        return new CheckPointBuilder(eventType);
    }

    /**
     * Retrieve a new {@link CheckPointBuilder} based on the given {@link CheckPoint},
     * i.e. copy its values and set them as default. Use this {@link CheckPointBuilder} to
     * modify any of those values.
     *
     * @param checkPoint the {@link CheckPoint} used as default value.
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
        optional = checkPoint.isOptional();
        label = checkPoint.getLabel();
    }

    public CheckPoint build() {
        CheckPointInternal cp = new CheckPointInternal();
        cp.eventType = eventType;
        cp.label = label;
        cp.match = match;
        cp.optional = optional;
        cp.set = new ArrayList<>(set);
        cp.validate = validate;
        return new CheckPoint(cp);
    }

    CheckPointBuilder optional() {
        optional = true;
        return this;
    }

    public CheckPointBuilder match(String match) {
        if (match == null) {
            throw nullArgumentError("match");
        }
        this.match = match;
        return this;
    }

    public CheckPointBuilder set(String... nameValuePairs) {
        for (String nameValuePair : nameValuePairs) {
            if (nameValuePair == null || nameValuePair.isEmpty() || !nameValuePair.contains("=")) {
                String message = "VarArg argument contains illegal value: %s";
                throw new NullPointerException(String.format(message, Arrays.toString(nameValuePairs)));
            }
        }
        set.addAll(Arrays.asList(nameValuePairs));
        return this;
    }

    public CheckPointBuilder validate(String validate) {
        if (validate == null) {
            throw nullArgumentError("validate");
        }
        this.validate = validate;
        return this;
    }

    public CheckPointBuilder label(String label) {
        if (label == null) {
            throw nullArgumentError("label");
        }
        this.label = label;
        return this;
    }

}
