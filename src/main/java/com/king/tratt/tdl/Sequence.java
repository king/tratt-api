package com.king.tratt.tdl;

import static com.king.tratt.tdl.SequenceBuilder.copyOf;

import java.util.ArrayList;
import java.util.List;

import com.king.tratt.tdl.TdlInternal.CheckPointInternal;
import com.king.tratt.tdl.TdlInternal.SequenceInternal;

public final class Sequence {

    SequenceInternal seqInternal;

    public enum Type {
        CONTAINER, FUNNEL, UNWANTED
    };

    Sequence(SequenceInternal seq) {
        seqInternal = seq;
    }

    // TODO return String?
    public Type getType() {
        return Type.valueOf(seqInternal.type.toUpperCase());
    }

    public String getMatch() {
        return seqInternal.match;
    }

    public String getSequenceMaxTime() {
        return seqInternal.sequenceMaxTime;
    }

    public String getName() {
        return seqInternal.name;
    }

    public List<CheckPoint> getCheckPoints() {
        List<CheckPoint> checkPoints = new ArrayList<>();
        for (CheckPointInternal cp : seqInternal.checkPoints) {
            checkPoints.add(new CheckPoint(cp));
        }
        return checkPoints;
    }

    @Override
    public String toString() {
        return seqInternal.toString();
    }

    /**
     * @param other
     * @return a new instance, which is a copy of this, but with the
     *         values of other merged into it.
     */
    public Sequence merge(Sequence other) {
        return SequenceBuilder.merge(copyOf(this), copyOf(other)).build();
    }
}
