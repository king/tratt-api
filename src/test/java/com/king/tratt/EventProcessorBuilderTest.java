// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt;

import static com.king.tratt.tdl.Tdl.fromPath;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.king.tratt.spi.test.imp.TestEventMetaDataFactory;
import com.king.tratt.tdl.Tdl;

public class EventProcessorBuilderTest {
    private EventProcessorBuilder builder = Tratt.newEventProcessorBuilder();
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void shouldThrowWhenNoEventMetaDataFactorySet() throws Exception {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("No 'EventMetaDataFactory' implementation found!");
        builder.start();
    }

    @Test
    public void shouldThrowWhenNoValueFactorySet() throws Exception {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("No 'ValueFactory' implementation found!");
        builder.setEventMetaDataFatory(new TestEventMetaDataFactory()).start();
    }

    @Test
    public void shouldThrowWhenAddingNullEventIterator() throws Exception {
        shouldThrowNpe("eventIterator", () -> {
            builder.addEventIterator(null);
        });
    }

    @Test
    public void shouldThrowWhenAddingNullProcessorListener() throws Exception {
        shouldThrowNpe("listener", () -> {
            builder.addProcessorListener(null);
        });
    }

    @Test
    public void shouldThrowWhenAddingNullSimpleProcessor() throws Exception {
        shouldThrowNpe("simpleProcessor", () -> {
            builder.addSimpleProcessor(null);
        });
    }

    @Test
    public void shouldThrowWhenAddingNullTdlList() throws Exception {
        shouldThrowNpe("tdls", () -> {
            builder.addTdls((List<Tdl>) null);
        });
    }

    @Test
    public void shouldThrowWhenAddingTdlListThatContainsAtLeastOneNullTdl() throws Exception {
        Tdl tdl = fromPath("classpath:com/king/tratt/acceptance.tdl");
        shouldThrowNpe("tdls", () -> {
            builder.addTdls(tdl, (Tdl) null);
        });
    }

    @Test
    public void shouldThrowWhenAddingNullTdlWithVarArg() throws Exception {
        shouldThrowNpe("tdls", () -> {
            builder.addTdls((Tdl) null);
        });
    }

    @Test
    public void shouldThrowWhenSettingNullApiConfigurationProvider() throws Exception {
        shouldThrowNpe("provider", () -> {
            builder.setApiConfigurationProvider(null);
        });
    }

    @Test
    public void shouldThrowWhenSettingNullValueFactory() throws Exception {
        shouldThrowNpe("valueFactory", () -> {
            builder.setValueFactory(null);
        });
    }

    @Test
    public void shouldThrowWhenSettingNullEventMetaDataFactory() throws Exception {
        shouldThrowNpe("mdFactory", () -> {
            builder.setEventMetaDataFatory(null);
        });
    }

    private void shouldThrowNpe(String argName, Procedure p) {
        expected.expect(NullPointerException.class);
        expected.expectMessage(argName);
        p.apply();
    }

    private static interface Procedure {
        void apply();
    }

}
