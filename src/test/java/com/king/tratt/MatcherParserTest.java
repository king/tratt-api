/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.spi.test.imp.TestEvent.fields;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.test.imp.TestEvent;
import com.king.tratt.spi.test.imp.TestEventMetaDataFactory;
import com.king.tratt.spi.test.imp.TestValueFactory;

public class MatcherParserTest {
    private ContextImp context = new ContextImp();
    private MatcherParser matcherParser;
    private Environment env = new Environment(new HashMap<>());
    private Matcher m;
    private TestEventMetaDataFactory mdFactory;

    @Before
    public void setup() throws Exception {
        mdFactory = new TestEventMetaDataFactory();
        TestValueFactory valueFactory = new TestValueFactory(mdFactory);
        matcherParser = new MatcherParser(valueFactory);
    }

    private Matcher matcher(String eventName, String expression) {
        EventMetaData eventMetaData = mdFactory.getEventMetaData(eventName);
        return matcherParser.parseMatcher(eventMetaData, expression, env);
    }

    @Test
    public void testMatchEquals() throws Exception {
        // given
        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");

        // when
        m = matcher("EventA", "a==1");

        // then
        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:event.a]]1 == [[source:constant]]1)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:event.a]]2 == [[source:constant]]1) << ");
        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
    }

    @Test
    public void testMatchLessEquals() throws Exception {
        // given
        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        // when
        m = matcher("EventB", "a<=2");

        // then
        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:event.a]]1 <= [[source:constant]]2)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo("([[source:event.a]]2 <= [[source:constant]]2)");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo(" >> ([[source:event.a]]5 <= [[source:constant]]2) << ");
        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test
    public void testMatchLess() throws Exception {
        // given
        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        // when
        m = matcher("EventB", "a<2");
        // then
        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:event.a]]1 < [[source:constant]]2)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:event.a]]2 < [[source:constant]]2) << ");
        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
    }

    @Test
    public void testMatchGreaterEquals() throws Exception {
        m = matcher("EventB", "a>=2");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo(" >> ([[source:event.a]]1 >= [[source:constant]]2) << ");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo("([[source:event.a]]2 >= [[source:constant]]2)");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:event.a]]5 >= [[source:constant]]2)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchGreater() throws Exception {
        m = matcher("EventB", "a>2");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo(" >> ([[source:event.a]]1 > [[source:constant]]2) << ");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:event.a]]2 > [[source:constant]]2) << ");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:event.a]]5 > [[source:constant]]2)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchLessEqualsReversed() throws Exception {
        m = matcher("EventB", "2>=a");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:constant]]2 >= [[source:event.a]]1)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo("([[source:constant]]2 >= [[source:event.a]]2)");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo(" >> ([[source:constant]]2 >= [[source:event.a]]5) << ");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test
    public void testMatchLessReversed() throws Exception {
        m = matcher("EventB", "2>a");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:constant]]2 > [[source:event.a]]1)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:constant]]2 > [[source:event.a]]2) << ");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
    }

    @Test
    public void testMatchGreaterEqualsReversed() throws Exception {
        m = matcher("EventB", "2<=a");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo(" >> ([[source:constant]]2 <= [[source:event.a]]1) << ");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo("([[source:constant]]2 <= [[source:event.a]]2)");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:constant]]2 <= [[source:event.a]]5)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchGreaterReversed() throws Exception {
        m = matcher("EventB", "2<a");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo(" >> ([[source:constant]]2 < [[source:event.a]]1) << ");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:constant]]2 < [[source:event.a]]2) << ");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:constant]]2 < [[source:event.a]]5)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchNotEquals() throws Exception {
        m = matcher("EventB", "a!=2");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:event.a]]1 != [[source:constant]]2)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:event.a]]2 != [[source:constant]]2) << ");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:event.a]]5 != [[source:constant]]2)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchNotEquals2() throws Exception {
        m = matcher("EventB", "!(a==2)");

        TestEvent e1 = fields("userid", "1", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "5", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo("!([[source:event.a]]1 == [[source:constant]]2)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> !([[source:event.a]]2 == [[source:constant]]2) << ");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("!([[source:event.a]]5 == [[source:constant]]2)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testAnd() throws Exception {
        m = matcher("EventB", "a==2 && b==3");

        TestEvent e1 = fields("userid", "2", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "2", "6", "15");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "(([[source:event.a]]2 == [[source:constant]]2) &&  >> ([[source:event.b]]'2' == [[source:constant]]3) << )");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "(([[source:event.a]]2 == [[source:constant]]2) && ([[source:event.b]]'3' == [[source:constant]]3))");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                "(([[source:event.a]]2 == [[source:constant]]2) &&  >> ([[source:event.b]]'6' == [[source:constant]]3) << )");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test
    public void testOr() throws Exception {
        m = matcher("EventB", "d==2 || e>=3");

        TestEvent e1 = fields("userid", "", "", "", "1", "2");
        TestEvent e2 = fields("userid", "", "", "", "1", "3");
        TestEvent e3 = fields("userid", "", "", "", "2", "6");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "( >> ([[source:event.d]]1 == [[source:constant]]2) <<  ||  >> ([[source:event.e]]2 >= [[source:constant]]3) << )");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "( >> ([[source:event.d]]1 == [[source:constant]]2) <<  || ([[source:event.e]]3 >= [[source:constant]]3))");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                "(([[source:event.d]]2 == [[source:constant]]2) || ([[source:event.e]]6 >= [[source:constant]]3))");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void test1eq1() throws Exception {
        m = matcher("EventA", "1==1");
        TestEvent e1 = fields("userid", "2", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "2", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:constant]]1 == [[source:constant]]1)");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo("([[source:constant]]1 == [[source:constant]]1)");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:constant]]1 == [[source:constant]]1)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void test1eq2NoMatches() throws Exception {
        m = matcher("EventA", "1==2");
        TestEvent e1 = fields("userid", "2", "2", "5");
        TestEvent e2 = fields("userid", "2", "3", "10");
        TestEvent e3 = fields("userid", "2", "6", "15");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo(" >> ([[source:constant]]1 == [[source:constant]]2) << ");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:constant]]1 == [[source:constant]]2) << ");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo(" >> ([[source:constant]]1 == [[source:constant]]2) << ");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test
    public void testAeqB() throws Exception {
        m = matcher("EventB", "b==c");
        TestEvent e1 = fields("userid", "", "2", "2");
        TestEvent e2 = fields("userid", "", "2", "3");
        TestEvent e3 = fields("userid", "", "6", "6");

        assertThat(m.toDebugString(e1, null))
                .isEqualTo("([[source:event.b]]'2' == [[source:event.c]]'2')");
        assertThat(m.toDebugString(e2, null))
                .isEqualTo(" >> ([[source:event.b]]'2' == [[source:event.c]]'3') << ");
        assertThat(m.toDebugString(e3, null))
                .isEqualTo("([[source:event.b]]'6' == [[source:event.c]]'6')");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchIntBooleanVariable() throws Exception {
        m = matcher("EventB", "a");

        TestEvent e1 = fields("userid", "5");
        TestEvent e2 = fields("userid", "0");
        TestEvent e3 = fields("userid", "1");

        assertThat(m.toDebugString(e1, null)).isEqualTo("(0 != [[source:event.a]]5)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(" >> (0 != [[source:event.a]]0) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo("(0 != [[source:event.a]]1)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testParseSpecialSignsInString() throws Exception {
        m = matcher("EventB", "c == \'apa-gris\'");

        TestEvent e1 = fields("userid", "2", "2", "apa-gris");
        TestEvent e2 = fields("userid", "2", "2", "gris-apa");
        TestEvent e3 = fields("userid", "2", "2", "'apa-gris'");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.c]]'apa-gris' == [[source:constant]]'apa-gris')");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.c]]'gris-apa' == [[source:constant]]'apa-gris') << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.c]]''apa-gris'' == [[source:constant]]'apa-gris') << ");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnquotedString() throws Exception {
        matcher("EventB", "c == ios");

    }

    @Test
    public void testBoolean() throws Exception {
        m = matcher("EventA", "c == true");
        TestEvent e1 = fields("userid", "2", "2", "true");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.c]]true == [[source:constant]]true)");
        assertThat(m.matches(e1, null)).isTrue();
    }

    @Test
    public void testParseSpaceSignsInString() throws Exception {
        m = matcher("EventB", "c == 'ios5,2 spec.'");

        TestEvent e1 = fields("userid", "2", "2", "ios5,2 spec.");
        TestEvent e2 = fields("userid", "2", "2", "ios5");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.c]]'ios5,2 spec.' == [[source:constant]]'ios5,2 spec.')");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.c]]'ios5' == [[source:constant]]'ios5,2 spec.') << ");
        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
    }

    @Test
    public void testMatchModulus() throws Exception {
        m = matcher("EventB", "a%10000 == 17");

        TestEvent e1 = fields("userid", "10017", "2", "3");
        TestEvent e2 = fields("userid", "17", "2", "3");
        TestEvent e3 = fields("userid", "27", "2", "3");
        TestEvent e4 = fields("userid", "30017", "2", "3");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "(([[source:event.a]]10017 % [[source:constant]]10000)17 == [[source:constant]]17)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "(([[source:event.a]]17 % [[source:constant]]10000)17 == [[source:constant]]17)");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> (([[source:event.a]]27 % [[source:constant]]10000)27 == [[source:constant]]17) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "(([[source:event.a]]30017 % [[source:constant]]10000)17 == [[source:constant]]17)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithPlus() throws Exception {
        m = matcher("EventB", "a+1 == d");

        TestEvent e1 = fields("userid", "1", "", "", "2");
        TestEvent e2 = fields("userid", "2", "", "", "2");
        TestEvent e3 = fields("userid", "3", "", "", "5");
        TestEvent e4 = fields("userid", "4", "", "", "5");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "(([[source:event.a]]1 + [[source:constant]]1)2 == [[source:event.d]]2)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> (([[source:event.a]]2 + [[source:constant]]1)3 == [[source:event.d]]2) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> (([[source:event.a]]3 + [[source:constant]]1)4 == [[source:event.d]]5) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "(([[source:event.a]]4 + [[source:constant]]1)5 == [[source:event.d]]5)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithMinus() throws Exception {
        m = matcher("EventB", "a == d-1");

        TestEvent e1 = fields("userid", "1", "", "", "2");
        TestEvent e2 = fields("userid", "2", "", "", "2");
        TestEvent e3 = fields("userid", "3", "", "", "5");
        TestEvent e4 = fields("userid", "4", "", "", "5");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]1 == ([[source:event.d]]2 - [[source:constant]]1)1)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.a]]2 == ([[source:event.d]]2 - [[source:constant]]1)1) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == ([[source:event.d]]5 - [[source:constant]]1)4) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]4 == ([[source:event.d]]5 - [[source:constant]]1)4)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithPrefixMinus() throws Exception {
        m = matcher("EventB", "a == -d*3");

        TestEvent e1 = fields("userid", "-9", "", "", "3");
        TestEvent e2 = fields("userid", "2", "", "", "3");
        TestEvent e3 = fields("userid", "3", "", "", "3");
        TestEvent e4 = fields("userid", "-3", "", "", "1");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]-9 == (([[source:constant]]0 - [[source:event.d]]3)-3 * [[source:constant]]3)-9)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.a]]2 == (([[source:constant]]0 - [[source:event.d]]3)-3 * [[source:constant]]3)-9) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == (([[source:constant]]0 - [[source:event.d]]3)-3 * [[source:constant]]3)-9) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]-3 == (([[source:constant]]0 - [[source:event.d]]1)-1 * [[source:constant]]3)-3)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithTimes() throws Exception {
        m = matcher("EventB", "a == d*3");

        TestEvent e1 = fields("userid", "1", "", "", "3");
        TestEvent e2 = fields("userid", "9", "", "", "3");
        TestEvent e3 = fields("userid", "3", "", "", "3");
        TestEvent e4 = fields("userid", "9", "", "", "3");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                " >> ([[source:event.a]]1 == ([[source:event.d]]3 * [[source:constant]]3)9) << ");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "([[source:event.a]]9 == ([[source:event.d]]3 * [[source:constant]]3)9)");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == ([[source:event.d]]3 * [[source:constant]]3)9) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]9 == ([[source:event.d]]3 * [[source:constant]]3)9)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithDivision() throws Exception {
        m = matcher("EventB", "a == d/3");

        TestEvent e1 = fields("userid", "1", "", "", "3");
        TestEvent e2 = fields("userid", "2", "", "", "9");
        TestEvent e3 = fields("userid", "3", "", "", "6");
        TestEvent e4 = fields("userid", "4", "", "", "12");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]1 == ([[source:event.d]]3 / [[source:constant]]3)1)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.a]]2 == ([[source:event.d]]9 / [[source:constant]]3)3) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == ([[source:event.d]]6 / [[source:constant]]3)2) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]4 == ([[source:event.d]]12 / [[source:constant]]3)4)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingContainingBrackets() throws Exception {
        m = matcher("EventB", "d == (a+1)*2");

        TestEvent e1 = fields("userid", "2", "", "", "3");
        TestEvent e2 = fields("userid", "2", "", "", "6");
        TestEvent e3 = fields("userid", "5", "", "", "6");
        TestEvent e4 = fields("userid", "5", "", "", "12");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                " >> ([[source:event.d]]3 == (([[source:event.a]]2 + [[source:constant]]1)3 * [[source:constant]]2)6) << ");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "([[source:event.d]]6 == (([[source:event.a]]2 + [[source:constant]]1)3 * [[source:constant]]2)6)");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.d]]6 == (([[source:event.a]]5 + [[source:constant]]1)6 * [[source:constant]]2)12) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.d]]12 == (([[source:event.a]]5 + [[source:constant]]1)6 * [[source:constant]]2)12)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingWithComplexExpression() throws Exception {
        m = matcher("EventB", "a%(d+1) == e/3 || e > 10");

        TestEvent e1 = fields("userid", "1", "", "", "2", "3");
        TestEvent e2 = fields("userid", "2", "", "", "2", "9");
        TestEvent e3 = fields("userid", "3", "", "", "5", "6");
        TestEvent e4 = fields("userid", "4", "", "", "5", "12");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "((([[source:event.a]]1 % ([[source:event.d]]2 + [[source:constant]]1)3)1 == ([[source:event.e]]3 / [[source:constant]]3)1) ||  >> ([[source:event.e]]3 > [[source:constant]]10) << )");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "( >> (([[source:event.a]]2 % ([[source:event.d]]2 + [[source:constant]]1)3)2 == ([[source:event.e]]9 / [[source:constant]]3)3) <<  ||  >> ([[source:event.e]]9 > [[source:constant]]10) << )");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                "( >> (([[source:event.a]]3 % ([[source:event.d]]5 + [[source:constant]]1)6)3 == ([[source:event.e]]6 / [[source:constant]]3)2) <<  ||  >> ([[source:event.e]]6 > [[source:constant]]10) << )");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "((([[source:event.a]]4 % ([[source:event.d]]5 + [[source:constant]]1)6)4 == ([[source:event.e]]12 / [[source:constant]]3)4) || ([[source:event.e]]12 > [[source:constant]]10))");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchWithSubStringFunction() throws Exception {
        TestEvent e = fields("userid", "", "abcdefgh");
        m = matcher("EventB", "substr(1,5,b) == 'bcde'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:substr(1, 5, 'abcdefgh')]]'bcde' == [[source:constant]]'bcde')");

        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithSubStringFunctionWhenError() throws Exception {
        TestEvent e = fields("userid", "", "abcdefgh");
        m = matcher("EventB", "substr(1, 'X', b) == 'bcde'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                " >> @CRASH due to 'java.lang.String cannot be cast to java.lang.Long' "
                        + "in 'substr('[[source:constant]]1', '[[source:constant]]'X'', 'stringEvent[2]')==[[source:constant]]'bcde''. "
                        + "See console log for more info. << ");

        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void testMatchWithSubStringFunction2() throws Exception {
        TestEvent e = fields("userid", "", "abcdefgh");
        m = matcher("EventB", "'bcde'==substr(1,5,b)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:constant]]'bcde' == [[source:substr(1, 5, 'abcdefgh')]]'bcde')");

        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithSubStringFunction3() throws Exception {
        TestEvent e = fields("userid", "", "abcdefgh");
        m = matcher("EventB", "'bcde'==substr(1,5,b) && b=='abcdefgh'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "(([[source:constant]]'bcde' == [[source:substr(1, 5, 'abcdefgh')]]'bcde') && ([[source:event.b]]'abcdefgh' == [[source:constant]]'abcdefgh'))");

        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunction() throws Exception {
        TestEvent e = fields("userid", "",
                "{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}");
        m = matcher("EventB", "jsonfield('o.p',b)=='panther'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:jsonfield('o.p', "
                        + "'{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}')]]'panther' == [[source:constant]]'panther')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunction2() throws Exception {
        TestEvent e = fields("userid", "",
                "{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}");
        m = matcher("EventB", "jsonfield('c',b)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "[[source:jsonfield('c', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}')]]true");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunction3() throws Exception {
        TestEvent e = fields("userid", "",
                "{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}");
        m = matcher("EventB", "jsonfield('c',b)>1233");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:jsonfield('c', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}')]]1234 > [[source:constant]]1233)");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunctionAndTdlVariables() throws Exception {
        String json = "{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}";
        TestEvent e = fields("userid", "", json);
        env.tdlVariables.put("$d", "4321");
        env.tdlVariables.put("$field", "c");
        m = matcher("EventB", "jsonfield($field,b)<$d");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:jsonfield('c', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}')]]1234 < [[source:constant]]4321)");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldWhenJsonPathIsIncorrect() throws Exception {
        TestEvent e = fields("userid", "",
                "{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}");
        m = matcher("EventB", "jsonfield('x',b)=='panther'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                " >> ([[source:jsonfield('x', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}')]]'"
                        + "[@ERROR incorrect json path: 'x']' == [[source:constant]]'panther') << ");
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void testMatchWithJsonFieldWhenJsonStringIsMalformed() throws Exception {
        TestEvent e = fields("userid", "", "{\"a\": ");
        m = matcher("EventB", "jsonfield('o.p',b)=='panther'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                " >> ([[source:jsonfield('o.p', '{\"a\": ')]]"
                        + "'[@ERROR malformed json string]' == [[source:constant]]'panther') << ");
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void testMatchWithJsonFieldWhenWrongNumberOfArguments() throws Exception {
        m = matcher("EventB", "jsonfield('o.p')=='panther'");
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithFields() throws Exception {
        TestEvent e = fields("userid", "", "abc", "123", "", "", "", "abc123");
        m = matcher("EventB", "g == concat(b,c)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:event.g]]'abc123' == [[source:concat('abc', '123')]]'abc123')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithFieldAndConstant() throws Exception {
        TestEvent e = fields("userid", "...", "123", "abc123");
        m = matcher("EventB", "c == concat('abc',b)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:event.c]]'abc123' == [[source:concat('abc', '123')]]'abc123')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithMoreThanTwoParams() throws Exception {
        TestEvent e = fields("userid", "", "123", "abc123def");
        m = matcher("EventB", "c == concat('abc',b, 'def')");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:event.c]]'abc123def' == [[source:concat('abc', '123', 'def')]]'abc123def')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithComma() throws Exception {
        TestEvent e = fields("userid", "", "123", "ab,g123");
        m = matcher("EventB", "c == concat('ab,g',b)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:event.c]]'ab,g123' == [[source:concat('ab,g', '123')]]'ab,g123')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithFieldAndVariable() throws Exception {
        TestEvent e = fields("userid", "", "123", "1234321");
        env.tdlVariables.put("$d", "4321");
        m = matcher("EventB", "c == concat(b, $d)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:event.c]]'1234321' == [[source:concat('123', '4321')]]'1234321')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingSplitFunctionWithFields() throws Exception {
        TestEvent e = fields("userid", "", "AAA,BBB,,DDD");
        m = matcher("EventB", "'DDD' == split(b, ',', 3)");

        String expected = "([[source:constant]]'DDD' == [[source:split('AAA,BBB,,DDD', ',', 3)]]'DDD')";
        assertThat(m.toDebugString(e, null))
                .isEqualTo(expected);
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canFailWhenUsingSplitFunctionWithFieldsAndIndexIsOutOfBounce() throws Exception {
        TestEvent e = fields("userid", "", "AAA,BBB");
        m = matcher("EventB", "'AAA' == split(b, ',', 2)");

        String expected = " >> ([[source:constant]]'AAA' == [[source:split('AAA,BBB', ',', 2)]]"
                + "'[@ERROR array index '2' out of bounce]') << ";
        assertThat(m.toDebugString(e, null))
                .isEqualTo(expected);
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void canCompareValuesWithEmptyString() throws Exception {
        m = matcher("EventB", "b == ''");

        TestEvent e = fields("userid", "", "AAA");
        String expected = " >> ([[source:event.b]]'AAA' == [[source:constant]]'') << ";
        String actual = m.toDebugString(e, null);
        assertThat(actual).isEqualTo(expected);
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void canCheckSufficientContextWhenCalculatingSum() throws Exception {
        TestEvent e = fields("userid", "6");
        context.set("ctx", 1L);
        env.sequenceVariables.put("ctx", null);
        m = matcher("EventB", "ctx + 5 == a");

        assertThat(m.hasSufficientContext(new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> @CRASH due to '[Insufficient Context!]' in '[[source:context.ctx]]+[[source:constant]]5==longEvent[1]'. "
                        + "See console log for more info. << ");

        assertThat(m.hasSufficientContext(context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:context.ctx]]1 + [[source:constant]]5)6 == [[source:event.a]]6)");
    }

    @Test
    public void canFailWhenCheckingNotEqualsEmptyString() throws Exception {
        TestEvent e = fields("userid", "4");
        context.set("ctx", 1L);
        env.sequenceVariables.put("ctx", null);
        m = matcher("EventB", "5 - ctx != ''");

        assertThat(m.hasSufficientContext(new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> @CRASH due to '[Insufficient Context!]' in '[[source:constant]]5-[[source:context.ctx]]!=[[source:constant]]'''. See console log for more info. << ");

        assertThat(m.hasSufficientContext(context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 - [[source:context.ctx]]1)4 != [[source:constant]]'')");
    }

    @Test
    public void canCheckSufficientContextWhenSubtracting() throws Exception {
        TestEvent e = fields("userid", "4");
        context.set("ctx", 1L);
        env.sequenceVariables.put("ctx", null);
        m = matcher("EventB", "5 - ctx == a");

        assertThat(m.hasSufficientContext(new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> @CRASH due to '[Insufficient Context!]' in '[[source:constant]]5-[[source:context.ctx]]==longEvent[1]'. See console log for more info. << ");

        assertThat(m.hasSufficientContext(context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 - [[source:context.ctx]]1)4 == [[source:event.a]]4)");
    }

    @Test
    public void canCheckSufficientContextWhenDividing() throws Exception {
        TestEvent e = fields("userid", "5");
        context.set("ctx", 1L);
        env.sequenceVariables.put("ctx", null);
        m = matcher("EventB", "5 / ctx == a");

        assertThat(m.hasSufficientContext(new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> @CRASH due to '[Insufficient Context!]' in '[[source:constant]]5/[[source:context.ctx]]==longEvent[1]'. "
                        + "See console log for more info. << ");

        assertThat(m.hasSufficientContext(context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 / [[source:context.ctx]]1)5 == [[source:event.a]]5)");
    }

    @Test
    public void canCheckSufficientContextWhenCalculatingModulus() throws Exception {
        TestEvent e = fields("userid", "1", "...");
        context.set("ctx", 2L);
        env.sequenceVariables.put("ctx", null);
        m = matcher("EventB", "5 % ctx == a");

        assertThat(m.hasSufficientContext(new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> @CRASH due to '[Insufficient Context!]' in '[[source:constant]]5%[[source:context.ctx]]==longEvent[1]'. See console log for more info. << ");
        assertThat(m.matches(e, new ContextImp())).isFalse();

        assertThat(m.hasSufficientContext(context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 % [[source:context.ctx]]2)1 == [[source:event.a]]1)");
        assertThat(m.matches(e, context)).isTrue();
    }

    @Test
    public void canCheckSufficientContextWhenMultiplying() throws Exception {
        TestEvent e = fields("userid", "15");
        context.set("ctx", 3L);
        env.sequenceVariables.put("ctx", null);
        m = matcher("EventB", "5 * ctx == a");

        assertThat(m.hasSufficientContext(new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> @CRASH due to '[Insufficient Context!]' in '[[source:constant]]5*[[source:context.ctx]]==longEvent[1]'. "
                        + "See console log for more info. << ");

        assertThat(m.hasSufficientContext(context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 * [[source:context.ctx]]3)15 == [[source:event.a]]15)");
    }

}
