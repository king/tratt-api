package com.king.tratt;

import org.junit.Test;

import com.king.tratt.spi.EventIterator;

public class AcceptanceTest {


    @Test
    public void testName() throws Exception {
        EventProcessorBuilder<MyEvent> b = Tratt.newEventProcessorBuilder();
        new EventProcessorBuilder<MyEvent>()
        .addEventIterator(new EventIterator<MyEvent>() {

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public void start() {
                // TODO Auto-generated method stub

            }

            @Override
            public MyEvent next() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean hasNext() {
                // TODO Auto-generated method stub
                return false;
            }
        });

    }

}
