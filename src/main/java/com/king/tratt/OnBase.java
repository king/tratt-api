package com.king.tratt;

import com.king.tratt.metadata.spi.Event;
import com.king.tratt.tdl.CheckPoint;

class OnBase<E extends Event> {
    private final String seqName;

    OnBase(String seqName) {
        this.seqName = seqName;
    }

    public String getSequenceName() {
        return seqName;
    }

    static class OnCheckPointBase<E extends Event> extends OnBase<E> {
        final CheckPointMatcher<E> cpMatcher;

        public OnCheckPointBase(String seqName, CheckPointMatcher<E> cpMatcher) {
            super(seqName);
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

        public OnCheckPointWithEventBase(String seqName, E event, CheckPointMatcher<E> cpMatcher) {
            super(seqName, cpMatcher);
            this.event = event;
        }

        public E getEvent() {
            return event;
        }
    }
}
