package com.king.tratt;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.tdl.CheckPoint;

class OnBase<E extends Event> {
    private final String seqName;
    private final Context context;

    OnBase(String seqName, Context context) {
        this.seqName = seqName;
        this.context = context;
    }

    final String getSequenceName() {
        return seqName;
    }

    final Context getContext() {
        return context;
    }

    static class OnCheckPointBase<E extends Event> extends OnBase<E> {
        final CheckPointMatcher<E> cpMatcher;

        OnCheckPointBase(String seqName, CheckPointMatcher<E> cpMatcher, Context context) {
            super(seqName, context);
            this.cpMatcher = cpMatcher;
        }

        int getSequenceIndex() {
            return cpMatcher.seqIndex;
        }

        int getCheckPointIndex() {
            return cpMatcher.cpIndex;
        }

        CheckPoint getCheckPoint() {
            return cpMatcher.checkPoint;
        }
    }

    static class OnCheckPointWithEventBase<E extends Event> extends OnCheckPointBase<E> {
        final E event;

        OnCheckPointWithEventBase(String seqName, E event, CheckPointMatcher<E> cpMatcher, Context context) {
            super(seqName, cpMatcher, context);
            this.event = event;
        }

        E getEvent() {
            return event;
        }
    }
}
