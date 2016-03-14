package com.king.tratt;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import com.king.tratt._temp.SequenceLogger;
import com.king.tratt.metadata.test.imp.TestEvent;
import com.king.tratt.metadata.test.imp.TestEventMetaDataFactory;
import com.king.tratt.metadata.test.imp.TestFromFileEventIterator;
import com.king.tratt.metadata.test.imp.TestValueFactory;
import com.king.tratt.tdl.Tdl;

public class AcceptanceTest {


    @Test
    public void testName() throws Exception {
        System.out.println(Thread.getAllStackTraces().keySet());

        //        StartedEventProcessor<TestEvent> started = Tratt.<TestEvent> newEventProcessorBuilder()
        //                .start();

        TestEventMetaDataFactory mdFactory = new TestEventMetaDataFactory();

        StartedEventProcessor<TestEvent> started = new EventProcessorBuilder<TestEvent>()
                //                .setApiConfiguratorProvider(new TestApiConfigurator())
                //
                //                .setApiConfiguration(new ApiConfigurator<>()
                //                        .setValueFactory(null)
                //                        .setEventMetaDataFatory(null)
                //                        .setCompletionStrategy(null))
                .addProcessorListener(new SequenceLogger<>())
                .setTimeout(5, SECONDS)
                .setValueFactory(new TestValueFactory(mdFactory))
                .setEventMetaDataFatory(mdFactory)
                // .addProcessorListener(null) // TODO should throw nullPpinter
                                            // exception!
                //                .setCompletionStrategy(null)
                .addEventIterator(new TestFromFileEventIterator("classpath:com/king/tratt/events.dat"))
                // .addVariable("varA", 222)
                // .addVariable("varB", 111111)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                // .addSimpleProcessor(e -> out.println("A " + e))
                // .addSimpleProcessor(e -> out.println("B " + e))
                .start();
        System.out.println(Thread.getAllStackTraces().keySet());

        started.awaitSuccess();

        started.shutdown();
        System.out.println(Thread.getAllStackTraces().keySet());

        //        CompletedEventProcessor completed = started.awaitCompletion();
        //        assertThat(completed.isValid()).isTrue();

    }

}
