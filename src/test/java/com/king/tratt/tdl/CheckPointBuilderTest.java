package com.king.tratt.tdl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CheckPointBuilderTest {

    private CheckPointBuilder builder = CheckPointBuilder.forEvent("");

    @Test
    public void canCopyExistingCheckPoint() throws Exception {
        // given
        CheckPoint checkPoint = CheckPointBuilder.forEvent("my-event")
                .match("fieldA==A")
                .validate("fieldB==B")
                .set("s=fieldA")
                .label("some-label")
                .optional()
                .build();

        // when
        CheckPoint copy = CheckPointBuilder.copyOf(checkPoint)
                .match("fieldB==B")
                .build();

        // then
        assertThat(copy.getEventType()).isEqualTo("my-event");
        assertThat(copy.getLabel()).isEqualTo("some-label");
        assertThat(copy.getMatch()).isEqualTo("fieldB==B");
        assertThat(copy.getSet()).containsOnly("s=fieldA");
        assertThat(copy.getValidate()).isEqualTo("fieldB==B");
        assertThat(copy.isOptional()).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenCallingMethodWithNullArgument_label() throws Exception {
        builder.label(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenCallingMethodWithNullArgument_match() throws Exception {
        builder.match(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenCallingMethodWithNullArgument_set() throws Exception {
        String s = null;
        builder.set(s);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenCallingMethodWithNullArgument_validate() throws Exception {
        builder.validate(null);
    }
}
