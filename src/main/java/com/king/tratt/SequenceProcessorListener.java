package com.king.tratt;

public interface SequenceProcessorListener<E extends Event> {

    default void onStart(OnStart<E> onStart) {
    };

    default void onEnd(OnEnd<E> onEnd) {
    };

    default void onSuccess(OnSuccess<E> onSuccess) {
    };

    default void onFailure(OnFailure<E> onFailure) {
    };

    default void onCheckPointTimeout(OnCheckPointTimeout<E> onTimeOut) {
    };

    default void onSequenceTimeout(OnSequenceTimeout<E> onTimeOut) {
    };

    default void onMatch(OnMatch<E> onMatch) {
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
