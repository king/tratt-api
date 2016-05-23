// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

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
