// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt.spi.test.imp;

import static java.util.Arrays.copyOf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import com.king.tratt.spi.Event;

public class TestEvent implements Event {

    private static SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSSZ");
    private String[] values;
    private String eventId;
    private long time;
    private String timestamp;

    public static TestEvent fields(String... values) {
        return new TestEvent("20151126T144726.849+0100", "1", values);
    }

    public TestEvent(String timestamp, String eventId, String[] values) {
        this.timestamp = timestamp;
        this.time = toTime(timestamp);
        this.eventId = eventId;
        this.values = copyOf(values, values.length);
    }

    @Override
    public String getId() {
        return eventId;
    }

    @Override
    public long getTimestampMillis() {
        return time;
    }

    synchronized private static long toTime(String timestamp) {
        try {
            // TODO use Java8 DateTime API?
            // Note: SimpleDateFormat is not thread safe.
            return DATE_FORMATER.parse(timestamp).getTime();
        } catch (ParseException e) {
            throw new RuntimeException("Can not parse to time: " + timestamp, e);
        }
    }

    public String getField(int index) {
        return values[index];
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", timestamp, eventId, Arrays.toString(values));
    }

}