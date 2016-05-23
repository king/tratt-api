// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt;

import static com.king.tratt.VariableParser.parse;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;

import org.junit.Test;

public class VariableParserTest {
    private static final String PREFIX = "";

    @Test
    public void shouldThrowWhenNameValuesIsNull() throws Exception {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> parse(PREFIX, (List<String>) null))
                .withMessage("'nameValues' must not be null!");
    }

    @Test
    public void shouldThrowWhenKeyPrefixIsNull() throws Exception {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> parse(null, asList()))
                .withMessage("'keyPrefix' must not be null!");
    }

    @Test
    public void shouldThrowWhenKeyIsEmpty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> parse(PREFIX, "=ab"))
                .withMessage("Name part cannot be empty string: '=ab'");
    }

    @Test
    public void shouldThrowWhenMissingEqualSign() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> parse(PREFIX, "ab"))
                .withMessage("Name/Value must be delimit by a '=' sign: 'ab'");
    }

    @Test
    public void shouldThrowWhenNameValueIsEmpty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> parse(PREFIX, ""))
                .withMessage("Name/Value cannot be an empty string: ''");
    }

    @Test
    public void shouldThrowWhenNameValueIsNull() throws Exception {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> parse(PREFIX, (String) null))
                .withMessage("Name/Value cannot be null: 'null'");
    }

    @Test
    public void canParseVariablesWhenSize3() throws Exception {
        assertThat(parse(asList("a=b", "c=d=e", "d=")))
                .containsOnly(entry("a", "b"), entry("c", "d=e"), entry("d", ""));
    }

    @Test
    public void canParseVariablesWhenSize1() throws Exception {
        assertThat(parse(asList("a=b")))
                .containsOnly(entry("a", "b"));
    }

    @Test
    public void canParseVariablesWhenSize2AndAddKeyPrefix() throws Exception {
        assertThat(parse("$", asList("a=b", "c=d")))
                .containsOnly(entry("$a", "b"), entry("$c", "d"));
    }

    @Test
    public void canParseVariablesWhenSize1AndAddKeyPrefix() throws Exception {
        assertThat(parse("$", asList("a=b")))
                .containsOnly(entry("$a", "b"));
    }

    @Test
    public void canParseVariablesWhenSize0() throws Exception {
        assertThat(parse(asList())).isEmpty();
    }

}
