package com.king.tratt;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import com.king.tratt.spi.test.imp.TestEventMetaDataFactory;
import com.king.tratt.spi.test.imp.TestFromFileEventIterator;
import com.king.tratt.spi.test.imp.TestValueFactory;
import com.king.tratt.tdl.Tdl;

public class AcceptanceTest {
    private static TestEventMetaDataFactory mdFactory = new TestEventMetaDataFactory();
    private static TestValueFactory valueFactory = new TestValueFactory(mdFactory);

    @Test
    public void canProcessEventsAccordingToTdl() throws Exception {
        TestFromFileEventIterator events = new TestFromFileEventIterator("classpath:com/king/tratt/events.dat");

        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setValueFactory(valueFactory)
                .setEventMetaDataFatory(mdFactory)
                .addEventIterator(events)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
    }

}
