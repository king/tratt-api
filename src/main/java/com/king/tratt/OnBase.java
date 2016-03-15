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

    public final String getSequenceName() {
        return seqName;
    }

    public final Context getContext() {
        return context;
    }

    static class OnCheckPointBase<E extends Event> extends OnBase<E> {
        final CheckPointMatcher<E> cpMatcher;

        public OnCheckPointBase(String seqName, CheckPointMatcher<E> cpMatcher, Context context) {
            super(seqName, context);
            this.cpMatcher = cpMatcher;
        }

        public int getSequenceIndex() {
            return cpMatcher.seqIndex;
        }

        public int getCheckPointIndex() {
            return cpMatcher.cpIndex;
        }

        public CheckPoint getCheckPoint() {
            return cpMatcher.checkPoint;
        }
    }

    static class OnCheckPointWithEventBase<E extends Event> extends OnCheckPointBase<E> {
        final E event;

        public OnCheckPointWithEventBase(String seqName, E event, CheckPointMatcher<E> cpMatcher, Context context) {
            super(seqName, cpMatcher, context);
            this.event = event;
        }

        public E getEvent() {
            return event;
        }
    }
}
