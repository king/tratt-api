/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
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

    final String getSequenceName() {
        return seqName;
    }

    final Context getContext() {
        return context;
    }

    static class OnCheckPointBase extends OnBase {
        final CheckPointMatcher cpMatcher;

        OnCheckPointBase(String seqName, CheckPointMatcher cpMatcher, Context context) {
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

    static class OnCheckPointWithEventBase extends OnCheckPointBase {
        final Event event;

        OnCheckPointWithEventBase(String seqName, Event event, CheckPointMatcher cpMatcher,
                Context context) {
            super(seqName, cpMatcher, context);
            this.event = event;
        }

        Event getEvent() {
            return event;
        }
    }
}
