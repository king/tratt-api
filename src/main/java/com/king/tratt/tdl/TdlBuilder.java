package com.king.tratt.tdl;

import static com.king.tratt.internal.Util.concat;
import static com.king.tratt.internal.Util.requireNonEmptyString;
import static com.king.tratt.internal.Util.requireNonNull;
import static com.king.tratt.internal.Util.requireNonNullElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TdlBuilder {
    String comment;
    Map<String, String> variables = new HashMap<>();
    Map<String, SequenceBuilder> sequenceBuilders = new LinkedHashMap<>();
    List<Tdl> addedTdls = new ArrayList<>();

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
        requireNonNull(comment, "comment");
        this.comment = comment;
        return this;
    }

    public TdlBuilder addVariable(String name, Integer value) {
        requireNonNull(value, "value");
        addVariable(name, String.valueOf(value));
        return this;
    }

    public TdlBuilder addVariable(String name, Long value) {
        requireNonNull(value, "value");
        addVariable(name, String.valueOf(value));
        return this;
    }

    public TdlBuilder addVariable(String name, String value) {
        requireNonNull(name, "name");
        requireNonNull(value, "value");
        requireNonEmptyString(name, "name");
        requireNonEmptyString(value, "value");
        variables.put(name, value);
        return this;
    }

    public TdlBuilder addSequence(SequenceBuilder sequenceBuilder) {
        requireNonNull(sequenceBuilder, "sequenceBuilder");
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
        requireNonNull(tdls, "tdls");
        requireNonNullElements(tdls, "tdls");
        if (tdls.isEmpty()) {
            throw new IllegalArgumentException("empty list of TDL files.");
        }
        addedTdls.addAll(tdls);
        return this;
    }
}
