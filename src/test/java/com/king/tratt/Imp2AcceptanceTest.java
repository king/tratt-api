package com.king.tratt;

import static com.king.tratt.tdl.Tdl.fromPath;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.test.imp2.FromJsonFileEventIterator;
import com.king.tratt.spi.test.imp2.JsonValue;

public class Imp2AcceptanceTest {

    @Test
    public void canProcessEventsAccordingToTdl() throws Exception {
        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(2, SECONDS)
                .addVariable("varA", "value-a0")
                .addVariable("varB", "value-b1")
                .addTdls(fromPath("classpath:com/king/tratt/spi/test/imp2/tdl.json"))
                .addEventIterator(
                        new FromJsonFileEventIterator("com/king/tratt/spi/test/imp2/events.json"))
                .setEventMetaDataFatory(new EventMetaDataFactory() {

                    @Override
                    public EventMetaData getEventMetaData(String eventName) {
                        return equalNameAndIdEventMetaData(eventName);
                    }
                })
                .setValueFactory((eventName, node) -> {
                    return new JsonValue(node);
                })
                .start();

        started.awaitSuccess();
    }

}
