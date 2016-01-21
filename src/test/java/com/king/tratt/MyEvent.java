package com.king.tratt;

import com.king.tratt.spi.Event;

public abstract class MyEvent implements Event {

    public abstract String getField(int index);

    static MyEvent fields(String... values) {
        return new MyEvent() {

            @Override
            public long getTimestamp() {
                return 0;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public String getField(int index) {
                return values[index];
            }
        };
    }

}
