// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt.tdl;

import static com.king.tratt.tdl.CheckPointBuilder.forEvent;
import static com.king.tratt.tdl.Sequence.Type.CONTAINER;
import static com.king.tratt.tdl.Sequence.Type.UNWANTED;
import static com.king.tratt.tdl.SequenceBuilder.copyOf;
import static com.king.tratt.tdl.SequenceBuilder.ofType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class SequenceBuilderTest {

    private SequenceBuilder builder = ofType(UNWANTED);

    @Test
    public void canMergeASequenceBuilderIntoSequenceBuilderInstance() throws Exception {
        // given
        SequenceBuilder sb1 = ofType(CONTAINER)
                .maxTime(2, TimeUnit.MINUTES)
                .name("nameA")
                .withCheckPoint(forEvent("ExternalStoreTransactionBegin"));

        SequenceBuilder sb2 = ofType(UNWANTED)
                .maxTime(1, TimeUnit.MINUTES)
                .name("nameB")
                .withCheckPoint(forEvent("ItemTransaction4"));

        // when
        Sequence merged = sb1.merge(sb2).build();

        // then
        assertThat(merged.getType()).isEqualTo(UNWANTED);
        assertThat(merged.getSequenceMaxTime()).isEqualTo("PT1M");
        assertThat(merged.getName()).isEqualTo("nameB");
        assertThat(merged.getCheckPoints()).hasSize(2);
    }

    @Test
    public void canMergeASequenceIntoSequenceInstance() throws Exception {
        // given
        SequenceBuilder sb1 = ofType(CONTAINER)
                .maxTime(2, TimeUnit.MINUTES)
                .name("nameA")
                .withCheckPoint(forEvent("ExternalStoreTransactionBegin"));
        Sequence seq1 = sb1.build();

        SequenceBuilder sb2 = ofType(UNWANTED)
                .maxTime(1, TimeUnit.MINUTES)
                .name("nameB")
                .withCheckPoint(forEvent("ItemTransaction4"));
        Sequence seq2 = sb2.build();

        // when
        Sequence merged = seq1.merge(seq2);

        // then
        assertThat(merged.getType()).isEqualTo(UNWANTED);
        assertThat(merged.getSequenceMaxTime()).isEqualTo("PT1M");
        assertThat(merged.getName()).isEqualTo("nameB");
        assertThat(merged.getCheckPoints()).hasSize(2);
    }

    @Test
    public void canMergeTwoSequences() throws Exception {
        // given
        SequenceBuilder sb1 = ofType(CONTAINER)
                .maxTime(2, TimeUnit.MINUTES)
                .name("nameA")
                .withCheckPoint(forEvent("ExternalStoreTransactionBegin"));

        SequenceBuilder sb2 = ofType(UNWANTED)
                .maxTime(1, TimeUnit.MINUTES)
                .name("nameB")
                .withCheckPoint(forEvent("ItemTransaction4"));

        // when
        Sequence merged = SequenceBuilder.merge(sb1, sb2).build();

        // then
        assertThat(merged.getType()).isEqualTo(UNWANTED);
        assertThat(merged.getSequenceMaxTime()).isEqualTo("PT1M");
        assertThat(merged.getName()).isEqualTo("nameB");
        assertThat(merged.getCheckPoints()).hasSize(2);
    }

    @Test
    public void canCopyExistingSequence() throws Exception {
        // given
        Sequence sequence = ofType(CONTAINER)
                .maxTime(2, TimeUnit.MINUTES)
                .name("name")
                .withCheckPoint(forEvent("ExternalStoreTransactionBegin"))
                .build();

        // when
        Sequence copy = copyOf(sequence).build();

        // then
        assertThat(copy.getType()).isEqualTo(CONTAINER);
        assertThat(copy.getSequenceMaxTime()).isEqualTo("PT2M");
        assertThat(copy.getName()).isEqualTo("name");
        assertThat(copy.getCheckPoints()).hasSize(1);
    }

    @Test
    public void canCopyExistingSequenceAndOverrideValues() throws Exception {
        // given
        Sequence sequence = ofType(UNWANTED)
                .maxTime(2, TimeUnit.MINUTES)
                .name("name")
                .withCheckPoint(forEvent("ExternalStoreTransactionBegin"))
                .build();

        // when
        Sequence copy = copyOf(sequence)
                .type(CONTAINER)
                .maxTime(3, TimeUnit.MINUTES)
                .name("new-name")
                .withCheckPoint(forEvent("ItemTransaction4"))
                .build();

        // then
        assertThat(copy.getType()).isEqualTo(CONTAINER);
        assertThat(copy.getSequenceMaxTime()).isEqualTo("PT3M");
        assertThat(copy.getName()).isEqualTo("new-name");

        assertThat(copy.getCheckPoints()).hasSize(2);
        List<String> eventTypes = new ArrayList<>();
        for (CheckPoint cp : copy.getCheckPoints()) {
            eventTypes.add(cp.getEventType());
        }
        assertThat(eventTypes).containsOnly("ItemTransaction4", "ExternalStoreTransactionBegin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenMaxTimeIsNegative() throws Exception {
        builder.maxTime(-1, TimeUnit.SECONDS);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMethodCalledWithNullArgument_maxTime() throws Exception {
        builder.maxTime(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMethodCalledWithNullArgument_name() throws Exception {
        builder.name(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMethodCalledWithNullArgument_withCheckpoint() throws Exception {
        builder.withCheckPoint(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMethodCalledWithNullArgument_type() throws Exception {
        builder.type(null);
    }

}
