package com.king.tratt;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.king.tratt.tdl.Tdl;
import com.king.tratt.test.imp.TestEvent;
import com.king.tratt.test.imp.TestEventMetaDataFactory;
import com.king.tratt.test.imp.TestFileEventIterator;
import com.king.tratt.test.imp.TestValueFactory;

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

                .setValueFactory(new TestValueFactory(mdFactory))
                .setEventMetaDataFatory(mdFactory)
                .setProcessListener(null)
                //                .setCompletionStrategy(null)
                .addEventIterator(new TestFileEventIterator("classpath:com/king/tratt/events.dat"))
                .addVariable("varA", 222)
                .addVariable("varB", 111111)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .addSimpleProcessor(e -> System.out.println("A " + e))
                .addSimpleProcessor(e -> System.out.println("B " + e))
                .start();
        System.out.println(Thread.getAllStackTraces().keySet());

        TimeUnit.SECONDS.sleep(5);
        started.shutdown();
        System.out.println(Thread.getAllStackTraces().keySet());

        //        CompletedEventProcessor completed = started.awaitCompletion();
        //        assertThat(completed.isValid()).isTrue();

    }

}
