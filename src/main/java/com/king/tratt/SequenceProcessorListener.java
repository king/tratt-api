package com.king.tratt;

import com.king.tratt.OnBase.OnCheckPointBase;
import com.king.tratt.OnBase.OnCheckPointWithEventBase;
import com.king.tratt.spi.Event;

public interface SequenceProcessorListener<E extends Event> {

    default void onSequenceStart(OnSequenceStart<E> onStart) {
    };

    default void onSequenceEnd(OnSequenceEnd<E> onEnd) {
    };

    default void onCheckPointSuccess(OnCheckPointSuccess<E> onSuccess) {
    };

    default void onCheckPointFailure(OnCheckPointFailure<E> onFailure) {
    };

    default void onCheckPointTimeout(OnCheckPointTimeout<E> onTimeout) {
    };

    default void onSequenceTimeout(OnSequenceTimeout<E> onTimeout) {
    };

    default void onCheckPointMatch(OnCheckPointMatch<E> onMatch) {
    };

    public static final class OnSequenceStart<E extends Event> extends OnBase<E> {
        OnSequenceStart(String seqName) {
            super(seqName);
        }
    }

    public static final class OnSequenceEnd<E extends Event> extends OnBase<E> {
        OnSequenceEnd(String seqName) {
            super(seqName);
        }
    }

    public static final class OnSequenceTimeout<E extends Event> extends OnBase<E> {
        OnSequenceTimeout(String seqName) {
            super(seqName);
        }
    }

    public static final class OnCheckPointMatch<E extends Event> extends OnCheckPointWithEventBase<E> {
        OnCheckPointMatch(String name, E event, CheckPointMatcher<E> cpMatcher) {
            super(name, event, cpMatcher);
        }
    }

    public static final class OnCheckPointSuccess<E extends Event> extends OnCheckPointWithEventBase<E> {
        OnCheckPointSuccess(String name, E event, CheckPointMatcher<E> cpMatcher) {
            super(name, event, cpMatcher);
        }
    }

    public static final class OnCheckPointFailure<E extends Event> extends OnCheckPointWithEventBase<E> {
        OnCheckPointFailure(String seqName, E event, CheckPointMatcher<E> cpMatcher) {
            super(seqName, event, cpMatcher);
        }

        public String getFailureString() {
            return cpMatcher.getDebugString(event);
        }
    }

    public static final class OnCheckPointTimeout<E extends Event> extends OnCheckPointBase<E> {
        OnCheckPointTimeout(String seqName, CheckPointMatcher<E> cpMatcher) {
            super(seqName, cpMatcher);
        }
    }
}
