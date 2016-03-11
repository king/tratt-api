package com.king.tratt;

import com.king.tratt.metadata.spi.Event;

public interface SequenceProcessorListener<E extends Event> {

    default void onSequenceStart(OnStart<E> onStart) {
    };

    default void onSequenceEnd(OnEnd<E> onEnd) {
    };

    default void onCheckPointSuccess(OnSuccess<E> onSuccess) {
    };

    default void onCheckPointFailure(OnFailure<E> onFailure) {
    };

    default void onCheckPointTimeout(OnCheckPointTimeout<E> onTimeout) {
    };

    default void onSequenceTimeout(OnSequenceTimeout<E> onTimeout) {
    };

    default void onCheckPointMatch(OnMatch<E> onMatch) {
    };

    static class Base<E extends Event> {
        private String seqName;

        public Base(String seqName) {
            this.seqName = seqName;
        }

        String getSequenceName() {
            return seqName;
        }
    }

    public static final class OnStart<E extends Event> extends Base<E> {
        public OnStart(String seqName) {
            super(seqName);
        }
    }

    public static final class OnEnd<E extends Event> extends Base<E> {
        public OnEnd(String seqName) {
            super(seqName);
        }
    }

    public static final class OnSuccess<E extends Event> extends Base<E> {
        public OnSuccess(String seqName) {
            super(seqName);
        }
    }

    public static final class OnFailure<E extends Event> extends Base<E> {
        public OnFailure(String seqName) {
            super(seqName);
        }
    }

    public static final class OnCheckPointTimeout<E extends Event> extends Base<E> {
        public OnCheckPointTimeout(String seqName) {
            super(seqName);
        }
    }

    public static final class OnSequenceTimeout<E extends Event> extends Base<E> {
        public OnSequenceTimeout(String seqName) {
            super(seqName);
        }
    }

    public static final class OnMatch<E extends Event> extends Base<E> {
        public OnMatch(String seqName) {
            super(seqName);
        }
    }

}
