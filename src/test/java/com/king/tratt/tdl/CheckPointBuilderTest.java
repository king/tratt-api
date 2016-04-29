/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
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
                .build();

        // when
        CheckPoint copy = CheckPointBuilder.copyOf(checkPoint)
                .match("fieldB==B")
                .build();

        // then
        assertThat(copy.getEventType()).isEqualTo("my-event");
        assertThat(copy.getMatch()).isEqualTo("fieldB==B");
        assertThat(copy.getSet()).containsOnly("s=fieldA");
        assertThat(copy.getValidate()).isEqualTo("fieldB==B");
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
