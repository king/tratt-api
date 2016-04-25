/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.tdl;

import static com.king.tratt.internal.Util.requireNonNull;
import static com.king.tratt.internal.Util.requireNoneNegative;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.king.tratt.tdl.Sequence.Type;
import com.king.tratt.tdl.TdlInternal.SequenceInternal;

public final class SequenceBuilder {

    private List<CheckPointBuilder> checkPointBuilders = new ArrayList<>();
    Type type;
    List<CheckPoint> checkPoints = new ArrayList<>();
    String name;
    String sequenceMaxTime = "pt15m";

    public static SequenceBuilder ofType(Type sequenceType) {
        return new SequenceBuilder(sequenceType);
    }

    public static SequenceBuilder copyOf(Sequence sequence) {
        return merge(ofType(sequence.getType()), sequence);
    }

    public static SequenceBuilder merge(SequenceBuilder b1, SequenceBuilder b2) {
        return merge(b1.build(), b2.build());

    }

    public static SequenceBuilder merge(Sequence s1, Sequence s2) {
        return merge(SequenceBuilder.copyOf(s1), s2);
    }

    private static SequenceBuilder merge(SequenceBuilder sb, Sequence seq) {
        sb.type = seq.getType();
        sb.name = seq.getName();
        sb.sequenceMaxTime = seq.getSequenceMaxTime();
        for (CheckPoint cp : seq.getCheckPoints()) {
            sb.withCheckPoint(CheckPointBuilder.copyOf(cp));
        }
        return sb;
    }

    private SequenceBuilder(Type type) {
        this.type = type;
    }

    /* For package private usage only */
    Sequence build() {
        SequenceInternal seq = new SequenceInternal();
        seq.name = name;
        seq.sequenceMaxTime = sequenceMaxTime;
        seq.type = type.toString();
        seq.checkPoints = new ArrayList<>();

        for (CheckPointBuilder builder : checkPointBuilders) {
            seq.checkPoints.add(builder.build().cpInternal);
        }
        Sequence result = new Sequence(seq);
        if (result.getName() == null || result.getName().isEmpty()) {
            throw new IllegalStateException("Sequence name must be set! " + result);
        }
        return result;
    }

    public SequenceBuilder name(String name) {
        requireNonNull(name, "name");
        this.name = name;
        return this;
    }

    public SequenceBuilder maxTime(long duration, TimeUnit timeUnit) {
        requireNoneNegative(duration, "duration");
        requireNonNull(timeUnit, "timeUnit");
        sequenceMaxTime = Duration.ofMillis(timeUnit.toMillis(duration)).toString();
        return this;
    }

    public SequenceBuilder withCheckPoint(CheckPointBuilder checkPointBuilder) {
        requireNonNull(checkPointBuilder, "checkPointBuilder");
        checkPointBuilders.add(checkPointBuilder);
        return this;
    }

    public SequenceBuilder type(Type type) {
        requireNonNull(type, "type");
        this.type = type;
        return this;
    }

    public SequenceBuilder merge(SequenceBuilder other) {
        return merge(other.build());
    }

    public SequenceBuilder merge(Sequence other) {
        SequenceBuilder.merge(this, other);
        return this;
    }
}
