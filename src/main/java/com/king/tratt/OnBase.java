// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.tdl.CheckPoint;

class OnBase {
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

    static class OnCheckPointBase extends OnBase {
        final CheckPointMatcher cpMatcher;

        OnCheckPointBase(String seqName, CheckPointMatcher cpMatcher, Context context) {
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

    static class OnCheckPointWithEventBase extends OnCheckPointBase {
        final Event event;

        OnCheckPointWithEventBase(String seqName, Event event, CheckPointMatcher cpMatcher,
                Context context) {
            super(seqName, cpMatcher, context);
            this.event = event;
        }

        public Event getEvent() {
            return event;
        }
    }
}
