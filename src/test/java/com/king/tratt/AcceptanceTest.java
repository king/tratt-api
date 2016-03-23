package com.king.tratt;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import com.king.tratt.spi.test.imp.TestEvent;
import com.king.tratt.spi.test.imp.TestEventMetaDataFactory;
import com.king.tratt.spi.test.imp.TestFromFileEventIterator;
import com.king.tratt.spi.test.imp.TestValueFactory;
import com.king.tratt.tdl.Tdl;

public class AcceptanceTest {

    @Test
    public void canProcessEventsAccordingToTdl() throws Exception {
        TestEventMetaDataFactory mdFactory = new TestEventMetaDataFactory();
        TestValueFactory tvFactory = new TestValueFactory(mdFactory);
        TestFromFileEventIterator events = new TestFromFileEventIterator("classpath:com/king/tratt/events.dat");

        StartedEventProcessor<TestEvent> started = new EventProcessorBuilder<TestEvent>()
                .setTimeout(5, SECONDS)
                .setValueFactory(tvFactory)
                .setEventMetaDataFatory(mdFactory)
                .addEventIterator(events)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
    }

}
