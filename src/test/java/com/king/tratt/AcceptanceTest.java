package com.king.tratt;

import static java.lang.System.out;
import static java.lang.Thread.getAllStackTraces;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.king.tratt.spi.ApiConfigurationProvider;
import com.king.tratt.spi.test.imp.TestApiConfigurationProvider;
import com.king.tratt.spi.test.imp.TestEvent;
import com.king.tratt.spi.test.imp.TestEventMetaDataFactory;
import com.king.tratt.spi.test.imp.TestFromFileEventIterator;
import com.king.tratt.spi.test.imp.TestValueFactory;
import com.king.tratt.tdl.Tdl;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class AcceptanceTest {
    TestEventMetaDataFactory mdFactory = new TestEventMetaDataFactory();
    TestValueFactory valueFactory = new TestValueFactory(mdFactory);
    TestFromFileEventIterator eventsFromFile = new TestFromFileEventIterator("classpath:com/king/tratt/events.dat");
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void canProcessEventsAccordingToTdl() throws Exception {
        System.out.println(getAllStackTraces().size() + " : " + getAllStackTraces());
        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .addEventIterator(eventsFromFile)
                .setEventMetaDataFatory(mdFactory)
                .setValueFactory(valueFactory)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
        System.out.println(getAllStackTraces().size() + " : " + getAllStackTraces());
    }

    @Test
    public void canUsePreprocessorWithSingleEventProcessorAndProcessEventsAccordingToTdl() throws Exception {
        Preprocessor pre = Tratt.newPreprocessor()
                .addEventIterator(eventsFromFile)
                .addSimpleProcessor(out::println)
                .start();

        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setEventMetaDataFatory(mdFactory)
                .setValueFactory(valueFactory)
                .setPreprocessor(pre)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
    }

    @Test
    public void canUsePreprocessorWithMultipleEventProcessorAndProcessEventsAccordingToTdl() throws Exception {
        Preprocessor preprocessor = Tratt.newPreprocessor()
                .addEventIterator(eventsFromFile)
                .addSimpleProcessor(e -> out.println("#" + e))
                .start();

        EventProcessorBuilder builder1 = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setComment("TDL1")
                .setEventMetaDataFatory(mdFactory)
                .setValueFactory(valueFactory)
                .setPreprocessor(preprocessor)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"));
        EventProcessorBuilder builder2 = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setComment("TDL2")
                .setEventMetaDataFatory(mdFactory)
                .setValueFactory(valueFactory)
                .setPreprocessor(preprocessor)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance-failing.tdl"));
        EventProcessorBuilder builder3 = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setComment("TDL3")
                .setEventMetaDataFatory(mdFactory)
                .setValueFactory(valueFactory)
                .setPreprocessor(preprocessor)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance-failing.tdl"));

        Multicaster multi = Tratt.newMulticaster(preprocessor, builder1, builder2, builder3);
        multi.awaitCompletion();
        assertThat(multi.getStartedEventProcessor(0).tdl.getComment()).isEqualTo("TDL1");
        assertThat(multi.getStartedEventProcessor(1).tdl.getComment()).isEqualTo("TDL2");
        assertThat(multi.getStartedEventProcessor(2).tdl.getComment()).isEqualTo("TDL3");
        assertThat(multi.isCompleted()).isTrue();
        assertThat(multi.getStartedEventProcessor(0).awaitCompletion().isValid()).isTrue();
        assertThat(multi.getStartedEventProcessor(1).awaitCompletion().isValid()).isFalse();
        assertThat(multi.getStartedEventProcessor(2).awaitCompletion().isValid()).isFalse();
        multi.shutdown();
    }

    @Test
    public void canSetApiConfigurationProviderExplicitly() throws Exception {
        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setApiConfigurationProvider(new TestApiConfigurationProvider())
                .addEventIterator(eventsFromFile)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
    }

    @Test
    public void canUseApiConfigurationProviderFromServiceLoader() throws Exception {

        TestEvent.fields();
        new MockUp<ServiceLoader<ApiConfigurationProvider>>() {
            @Mock
            public Iterator<? extends ApiConfigurationProvider> iterator() {
                return Arrays.asList(new TestApiConfigurationProvider()).iterator();
            }
        };

        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .addEventIterator(eventsFromFile)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
    }

}
