package com.king.tratt.test.imp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import com.king.tratt.metadata.spi.Event;

public class TestEvent implements Event {

    private static SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSSZ");
    private String[] values;
    private long eventId;
    private String timestamp;

    public static TestEvent fields(String... values) {
        return new TestEvent("-9999", "1", values);
    }

    public TestEvent(String timestamp, String eventId, String[] values) {
        this.timestamp = timestamp;
        this.eventId = Long.parseLong(eventId);
        this.values = values;
    }

    @Override
    public long getId() {
        return eventId;
    }

    @Override
    public long getTimestampMillis() {
        try {
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