package com.king.tratt.tdl;

import static com.king.tratt.tdl.Util.concat;
import static com.king.tratt.tdl.Util.containsEitherNullOrEmptyStringElements;
import static com.king.tratt.tdl.Util.nullArgumentError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.king.tratt.tdl.Sequence.Type;

public final class TdlBuilder {
    String comment;
    Map<String, String> variables = new HashMap<>();
    Map<String, SequenceBuilder> sequenceBuilders = new LinkedHashMap<>();

    Tdl useTdl;
    List<Tdl> addedTdls = new ArrayList<>();
    List<String> matchExpressions = new ArrayList<>();
    Type sequencesType;
    Long sequencesMaxTimeMillis;

    TdlBuilder() {
        /* for package private usage only */
    }

    /**
     * @return TdlFile
     */
    public Tdl build() {
        return new Tdl(this);
    }

    public TdlBuilder setComment(String comment) {
        if (comment == null) {
            throw nullArgumentError("comment");
        }
        this.comment = comment;
        return this;
    }

    public TdlBuilder addVariable(String name, Integer value) {
        if (value == null) {
            throw nullArgumentError("value");
        }
        addVariable(name, String.valueOf(value));
        return this;
    }

    public TdlBuilder addVariable(String name, Long value) {
        if (value == null) {
            throw nullArgumentError("value");
        }
        addVariable(name, String.valueOf(value));
        return this;
    }

    public TdlBuilder addVariable(String name, String value) {
        if (name == null) {
            throw nullArgumentError("name");
        }
        if (value == null) {
            throw nullArgumentError("value");
        }
        if (name.isEmpty()) {
            throw Util.emptyStringArgumentError("name");
        }
        if (value.isEmpty()) {
            throw Util.emptyStringArgumentError("value");
        }
        variables.put(name, value);
        return this;
    }

    public TdlBuilder addSequence(SequenceBuilder sequenceBuilder) {
        if (sequenceBuilder == null) {
            throw nullArgumentError("sequenceBuilder");
        }
        if (sequenceBuilder.name == null || sequenceBuilder.name.isEmpty()) {
            throw new IllegalArgumentException("Sequence name must be set! " + sequenceBuilder.name);
        }
        sequenceBuilders.put(sequenceBuilder.name, sequenceBuilder);
        return this;
    }

    public TdlBuilder addTdls(Tdl first, Tdl... rest) {
        return addTdls(concat(first, rest));
    }

    public TdlBuilder addTdls(List<Tdl> tdls) {
        if (tdls == null) {
            throw nullArgumentError("tdls");
        }
        if (tdls.isEmpty()) {
            throw new IllegalArgumentException("empty list of TDL files.");
        }
        if (containsEitherNullOrEmptyStringElements(tdls)) {
            RuntimeException ex = Util.varArgError(tdls);
            throw ex;
        }
        addedTdls.addAll(tdls);
        return this;
    }

    /**
     * Prepend the existing match field on all {@link CheckPoint}'s in this {@link TdlFile} with
     * the given {@code match}.
     *
     * @param match
     * @return
     */
    public TdlBuilder addMatch(String match) {
        if (match == null) {
            throw nullArgumentError("match");
        }
        matchExpressions.add(match);
        return this;
    }

    public TdlBuilder addCoreUserIdFilter(long... coreUserIds) {
        if (coreUserIds == null) {
            throw nullArgumentError("coreUserIds");
        }
        if (coreUserIds.length == 0) {
            throw new IllegalArgumentException("No argument given.");
        }
        if (coreUserIds.length == 1) {
            addMatch("coreUserId==" + String.valueOf(coreUserIds[0]));
        } else {
            String arrayToString = Arrays.toString(coreUserIds).replaceAll(" ", "");
            addMatch(String.format("coreUserId in %s", arrayToString));
        }
        return this;
    }

    /**
     * Overrides the type of all {@link Sequence}'s in this {@link TdlBuilder} to
     * the given {@code sequenceType}.
     */
    public TdlBuilder setSequencesType(Type sequenceType) {
        if (sequenceType == null) {
            throw nullArgumentError("sequenceType");
        }
        sequencesType = sequenceType;
        return this;
    }

    /**
     * Overrides the max duration of all {@link Sequence}'s in this {@link TdlBuilder} to
     * the given duration.
     */
    public TdlBuilder setSequencesMaxTime(long duration, TimeUnit timeUnit) {
        if (duration < 0) {
            String message = "Argument 'duration' is negative.";
            throw new IllegalArgumentException(message);
        }
        if (timeUnit == null) {
            throw nullArgumentError("timeUnit");
        }
        sequencesMaxTimeMillis = timeUnit.toMillis(duration);
        return this;
    }


}
