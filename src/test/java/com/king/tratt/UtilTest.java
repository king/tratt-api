/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.king.tratt.spi.Value;
import com.king.tratt.spi.test.imp.TestEvent;

@RunWith(MockitoJUnitRunner.class)
public class UtilTest {

    @Mock
    Value v1;
    @Mock
    Value v2;
    @Mock
    Value v3;

    @Test
    public void canReplaceConversionCharacters() throws Exception {
        TestEvent e = TestEvent.fields("a", "b");
        Mockito.when(v1.asString(e, null)).thenReturn("B");
        Mockito.when(v2.toDebugString(e, null)).thenReturn("DDD");
        Mockito.when(v3.get(e, null)).thenReturn("5");

        assertThat(util.format(e, null, "A ~v ~s ~d ~q END", v1, "C", v2, v3))
        .isEqualTo("A B C DDD 5 END");
    }

    @Test
    public void canHandleWhenNoConversionCharacterIsPresent() throws Exception {
        assertThat(util.format(null, null, "A B C")).isEqualTo("A B C");
    }

    @Test
    public void canJoinConversion() throws Exception {
        TestEvent e = TestEvent.fields("a", "b");
        Mockito.when(v1.get(e, null)).thenReturn("A");
        Mockito.when(v2.get(e, null)).thenReturn("5");
        Mockito.when(v3.get(e, null)).thenReturn("C");

        assertThat(util.formatJoin(e, null, ", ", "~q", Arrays.asList(v1, v2, v3)))
        .isEqualTo("'A', 5, 'C'");
    }

}
