package com.king.tratt;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.atomic.AtomicBoolean;

class TimeoutChecker {
    private final AtomicBoolean hasTimeoutOccured = new AtomicBoolean(false);

    TimeoutChecker(StartedEventProcessor started) {
        started.executor.execute(() -> {
            try {
                SECONDS.sleep(started.timeoutSeconds);
                hasTimeoutOccured.set(true);
            } catch (InterruptedException e) {
                // shutdown gracefully
            }
        });
    }

    boolean hasOccured() {
        return hasTimeoutOccured.get();
    }

}
