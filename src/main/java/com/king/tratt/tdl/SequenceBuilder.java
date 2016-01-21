package com.king.tratt.tdl;

import static com.king.tratt.tdl.Util.nullArgumentError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;

import com.king.tratt.tdl.Sequence.Type;
import com.king.tratt.tdl.TdlInternal.SequenceInternal;

public final class SequenceBuilder {

    private List<CheckPointBuilder> checkPointBuilders = new ArrayList<>();
    Type type;
    List<CheckPoint> checkPoints = new ArrayList<>();
    String name;
    String sequenceMaxTime = "pt15m";
    String match = "";

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
        sb.match = seq.getMatch();
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
        seq.match = match;
        seq.name = name;
        seq.sequenceMaxTime = sequenceMaxTime;
        seq.type = type.toString();
        seq.checkPoints = new ArrayList<>();

        for (CheckPointBuilder builder : checkPointBuilders) {
            if (!match.isEmpty()) {
                prependMatch(builder);
            }
            seq.checkPoints.add(builder.build().cpInternal);
        }
        Sequence result = new Sequence(seq);
        if (result.getName() == null || result.getName().isEmpty()) {
            throw new IllegalStateException("Sequence name must be set! " + result);
        }
        return result;
    }

    private void prependMatch(CheckPointBuilder builder) {
        StringBuilder newMatch = new StringBuilder(match);
        if (!builder.match.isEmpty()) {
            newMatch.append(" && ").append(builder.match);
        }
        builder.match = newMatch.toString();
    }

    public SequenceBuilder name(String name) {
        if (name == null) {
            throw nullArgumentError("name");
        }
        this.name = name;
        return this;
    }

    public SequenceBuilder maxTime(long duration, TimeUnit timeUnit) {
        if (duration < 0) {
            String message = "Argument 'duration' is negative.";
            throw new IllegalArgumentException(message);
        }
        if (timeUnit == null) {
            throw nullArgumentError("timeUnit");
        }
        sequenceMaxTime = Duration.millis(timeUnit.toMillis(duration)).toString();
        return this;
    }

    public SequenceBuilder withCheckPoint(CheckPointBuilder checkPointBuilder) {
        if (checkPointBuilder == null) {
            throw nullArgumentError("builder");
        }
        checkPointBuilders.add(checkPointBuilder);
        return this;
    }

    public SequenceBuilder match(String match) {
        if (match == null) {
            throw nullArgumentError("match");
        }
        this.match = match;
        return this;
    }

    public SequenceBuilder type(Type type) {
        if (type == null) {
            throw nullArgumentError("type");
        }
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
