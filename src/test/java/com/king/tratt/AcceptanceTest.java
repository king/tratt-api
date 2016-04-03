package com.king.tratt;

import static java.lang.System.out;
import static java.lang.Thread.getAllStackTraces;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.king.tratt.spi.ApiConfigurationProvider;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.ValueFactory;
import com.king.tratt.spi.test.imp.TestEventMetaDataFactory;
import com.king.tratt.spi.test.imp.TestFromFileEventIterator;
import com.king.tratt.spi.test.imp.TestValueFactory;
import com.king.tratt.tdl.Tdl;

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
        ApiConfigurationProvider provider = new ApiConfigurationProvider() {

            @Override
            public ValueFactory valueFactory() {
                return (eventName, parameterName) -> {
                    throw new TrattException("valueFactory");
                };
            }

            @Override
            public EventMetaDataFactory metaDataFactory() {
                return eventName -> {
                    throw new TrattException("metaDataFactory");
                };
            }
        };
        expected.expect(TrattException.class);
        expected.expectMessage("metYYY");

        StartedEventProcessor started = Tratt.newEventProcessorBuilder()
                .setTimeout(5, SECONDS)
                .setApiConfigurationProvider(provider)
                .addEventIterator(eventsFromFile)
                .addTdls(Tdl.fromPath("classpath:com/king/tratt/acceptance.tdl"))
                .start();
        started.awaitSuccess();
    }

}
