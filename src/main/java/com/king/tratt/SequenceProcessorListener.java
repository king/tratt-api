// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import com.king.tratt.OnBase.OnCheckPointBase;
import com.king.tratt.OnBase.OnCheckPointWithEventBase;
import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;

public interface SequenceProcessorListener {

    default void onSequenceStart(OnSequenceStart onStart) {
    };

    default void onSequenceEnd(OnSequenceEnd onEnd) {
    };

    default void onCheckPointSuccess(OnCheckPointSuccess onSuccess) {
    };

    default void onCheckPointFailure(OnCheckPointFailure onFailure) {
    };

    default void onCheckPointTimeout(OnCheckPointTimeout onTimeout) {
    };

    default void onSequenceTimeout(OnSequenceTimeout onTimeout) {
    };

    default void onCheckPointMatch(OnCheckPointMatch onMatch) {
    };

    public static final class OnSequenceStart extends OnBase {
        OnSequenceStart(String seqName, Context context) {
            super(seqName, context);
        }
    }

    public static final class OnSequenceEnd extends OnBase {
        OnSequenceEnd(String seqName, Context context) {
            super(seqName, context);
        }
    }

    public static final class OnSequenceTimeout extends OnBase {
        OnSequenceTimeout(String seqName, Context context) {
            super(seqName, context);
        }
    }

    public static final class OnCheckPointMatch extends OnCheckPointWithEventBase {
        OnCheckPointMatch(String name, Event event, CheckPointMatcher cpMatcher, Context context) {
            super(name, event, cpMatcher, context);
        }
    }

    public static final class OnCheckPointSuccess extends OnCheckPointWithEventBase {
        OnCheckPointSuccess(String name, Event event, CheckPointMatcher cpMatcher,
                Context context) {
            super(name, event, cpMatcher, context);
        }
    }

    public static final class OnCheckPointFailure extends OnCheckPointWithEventBase {
        OnCheckPointFailure(String seqName, Event event, CheckPointMatcher cpMatcher,
                Context context) {
            super(seqName, event, cpMatcher, context);
        }

        public String getFailureString() {
            return cpMatcher.getDebugString(event, getContext());
        }
    }

    public static final class OnCheckPointTimeout extends OnCheckPointBase {
        OnCheckPointTimeout(String seqName, CheckPointMatcher cpMatcher, Context context) {
            super(seqName, cpMatcher, context);
        }
    }
}
