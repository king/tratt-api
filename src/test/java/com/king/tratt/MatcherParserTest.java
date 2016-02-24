package com.king.tratt;

import static com.king.tratt.test.imp.TestEvent.fields;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.king.tratt.spi.EventMetaData;
import com.king.tratt.test.imp.TestApiConfigurator;
import com.king.tratt.test.imp.TestEvent;
import com.king.tratt.test.imp.TestValueFactory;

// @RunWith(MockitoJUnitRunner.class)
public class MatcherParserTest {

    private MatcherParser<TestEvent> matcherParser;
    private ContextImp context = new ContextImp();
    private Environment env = new Environment(context);
    private TestApiConfigurator confProvider = new TestApiConfigurator();
    private Matcher<TestEvent> m;

    @Before
    public void setup() throws Exception {
        TestValueFactory f = confProvider.getValueFactory();
        //        TestValueFactory f = confProvider.getValueFactory();
        matcherParser = new MatcherParser<>(new TdlNodeParser(), f, new FunctionFactoryProvider<>());
    }

    private Matcher<TestEvent> matcher(String eventName, String expression) {
        EventMetaData eventMetaData = confProvider.getEventMetaDataFactory().getEventMetaData(eventName);
        return matcherParser.parseMatcher(eventMetaData, expression, env);
    }

    @Test
    public void testMatchEquals() throws Exception {
        // given
        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");

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
        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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
        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("5", "6", "15");

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

        TestEvent e1 = fields("2", "2", "5");
        TestEvent e2 = fields("2", "3", "10");
        TestEvent e3 = fields("2", "6", "15");

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
        m = matcher("EventB", "a==2 || b>=3");

        TestEvent e1 = fields("1", "2", "5");
        TestEvent e2 = fields("1", "3", "10");
        TestEvent e3 = fields("2", "6", "15");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "( >> ([[source:event.a]]1 == [[source:constant]]2) <<  ||  >> ([[source:event.b]]2 >= [[source:constant]]3) << )");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "( >> ([[source:event.a]]1 == [[source:constant]]2) <<  || ([[source:event.b]]3 >= [[source:constant]]3))");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                "(([[source:event.a]]2 == [[source:constant]]2) || ([[source:event.b]]6 >= [[source:constant]]3))");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void test1eq1() throws Exception {
        m = matcher("AbTestCaseAssigned", "1==1");
        MyEvent e1 = fields("2", "2", "5");
        MyEvent e2 = fields("2" , "3", "10");
        MyEvent e3 = fields("2" , "6", "15");

        assertThat(m.toDebugString(e1, null)).isEqualTo("([[source:constant]]1 == [[source:constant]]1)");
        assertThat(m.toDebugString(e2, null)).isEqualTo("([[source:constant]]1 == [[source:constant]]1)");
        assertThat(m.toDebugString(e3, null)).isEqualTo("([[source:constant]]1 == [[source:constant]]1)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void test1eq2NoMatches() throws Exception {
        m = matcher("AbTestCaseAssigned", "1==2");
        MyEvent e1 = fields("2", "2", "5");
        MyEvent e2 = fields("2" , "3", "10");
        MyEvent e3 = fields("2" , "6", "15");

        assertThat(m.toDebugString(e1, null)).isEqualTo(" >> ([[source:constant]]1 == [[source:constant]]2) << ");
        assertThat(m.toDebugString(e2, null)).isEqualTo(" >> ([[source:constant]]1 == [[source:constant]]2) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(" >> ([[source:constant]]1 == [[source:constant]]2) << ");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test
    public void testAeqB() throws Exception {
        m = matcher("AbTestCaseAssigned", "a==b");
        MyEvent e1 = fields("2", "2", "5");
        MyEvent e2 = fields("2" , "3", "10");
        MyEvent e3 = fields("6" , "6", "15");

        assertThat(m.toDebugString(e1, null)).isEqualTo("([[source:event.a]]2 == [[source:event.b]]2)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(" >> ([[source:event.a]]2 == [[source:event.b]]3) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo("([[source:event.a]]6 == [[source:event.b]]6)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testMatchIntBooleanVariable() throws Exception {
        m = matcher("AbTestCaseAssigned", "c");

        MyEvent e1 = fields("2", "2", "5");
        MyEvent e2 = fields("2" , "3", "0");
        MyEvent e3 = fields("2" , "6", "1");

        assertThat(m.toDebugString(e1, null)).isEqualTo("(0 != [[source:event.c]]5)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(" >> (0 != [[source:event.c]]0) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo("(0 != [[source:event.c]]1)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isTrue();
    }

    @Test
    public void testParseSpecialSignsInString() throws Exception {
        m = matcher("AbTestCaseAssigned", "c == \'apa-gris\'");

        MyEvent e1 = fields("2", "2", "apa-gris");
        MyEvent e2 = fields("2", "2", "gris-apa");
        MyEvent e3 = fields("2", "2", "'apa-gris'");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.c]]apa-gris == [[source:constant]]'apa-gris')");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.c]]gris-apa == [[source:constant]]'apa-gris') << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.c]]'apa-gris' == [[source:constant]]'apa-gris') << ");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnquotedString() throws Exception {
        matcher("AbTestCaseAssigned", "c == ios");

    }

    @Test
    public void testBoolean() throws Exception {
        m = matcher("AbTestCaseAssigned", "c == true");
        MyEvent e1 = fields("2", "2", "true");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.c]]true == [[source:constant]]true)");
        assertThat(m.matches(e1, null)).isTrue();
    }

    @Test
    public void testParseSpaceSignsInString() throws Exception {
        m = matcher("AbTestCaseAssigned", "c == 'ios5,2 spec.'");

        MyEvent e1 = fields("2", "2", "ios5,2 spec.");
        MyEvent e2 = fields("2", "2", "ios5");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.c]]ios5,2 spec. == [[source:constant]]'ios5,2 spec.')");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.c]]ios5 == [[source:constant]]'ios5,2 spec.') << ");
        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
    }

    @Test
    public void testMatchModulus() throws Exception {
        m = matcher("AbTestCaseAssigned", "a%10000 == 17");

        MyEvent e1 = fields("10017", "2", "3");
        MyEvent e2 = fields("17", "2", "3");
        MyEvent e3 = fields("27", "2", "3");
        MyEvent e4 = fields("30017", "2", "3");

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
    public void testMatchIn() throws Exception {
        m = matcher("AbTestCaseAssigned", "a in [1,b,'three',5]");

        MyEvent e0 = fields("0", "2", "3");
        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("2", "2", "3");
        MyEvent e3 = fields("three", "2", "3");
        MyEvent e4 = fields("4", "2", "3");
        MyEvent e5 = fields("5", "2", "3");
        MyEvent e6 = fields("6", "2", "3");

        assertThat(m.toDebugString(e0, null)).isEqualTo(
                " >> ([[source:event.a]]0 IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5]) << ");
        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]1 IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5])");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "([[source:event.a]]2 IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5])");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                "([[source:event.a]]three IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5])");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                " >> ([[source:event.a]]4 IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5]) << ");
        assertThat(m.toDebugString(e5, null)).isEqualTo(
                "([[source:event.a]]5 IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5])");
        assertThat(m.toDebugString(e6, null)).isEqualTo(
                " >> ([[source:event.a]]6 IN [[[source:constant]]1, [[source:event.b]]2, [[source:constant]]'three', [[source:constant]]5]) << ");

        assertThat(m.matches(e0, null)).isFalse();
        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isTrue();
        assertThat(m.matches(e4, null)).isFalse();
        assertThat(m.matches(e5, null)).isTrue();
        assertThat(m.matches(e6, null)).isFalse();
    }

    @Test
    public void testMatchingExpressionWithPlus() throws Exception {
        m = matcher("AbTestCaseAssigned", "a+1 == b");

        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("2", "2", "3");
        MyEvent e3 = fields("3", "5", "3");
        MyEvent e4 = fields("4", "5", "3");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "(([[source:event.a]]1 + [[source:constant]]1)2 == [[source:event.b]]2)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> (([[source:event.a]]2 + [[source:constant]]1)3 == [[source:event.b]]2) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> (([[source:event.a]]3 + [[source:constant]]1)4 == [[source:event.b]]5) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "(([[source:event.a]]4 + [[source:constant]]1)5 == [[source:event.b]]5)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithMinus() throws Exception {
        m = matcher("AbTestCaseAssigned", "a == b-1");

        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("2", "2", "3");
        MyEvent e3 = fields("3", "5", "3");
        MyEvent e4 = fields("4", "5", "3");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]1 == ([[source:event.b]]2 - [[source:constant]]1)1)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.a]]2 == ([[source:event.b]]2 - [[source:constant]]1)1) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == ([[source:event.b]]5 - [[source:constant]]1)4) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]4 == ([[source:event.b]]5 - [[source:constant]]1)4)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithPrefixMinus() throws Exception {
        m = matcher("AbTestCaseAssigned", "a == -c*3");

        MyEvent e1 = fields("-9", "2", "3");
        MyEvent e2 = fields("2", "2", "3");
        MyEvent e3 = fields("3", "5", "3");
        MyEvent e4 = fields("-3", "5", "1");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]-9 == (([[source:constant]]0 - [[source:event.c]]3)-3 * [[source:constant]]3)-9)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.a]]2 == (([[source:constant]]0 - [[source:event.c]]3)-3 * [[source:constant]]3)-9) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == (([[source:constant]]0 - [[source:event.c]]3)-3 * [[source:constant]]3)-9) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]-3 == (([[source:constant]]0 - [[source:event.c]]1)-1 * [[source:constant]]3)-3)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithTimes() throws Exception {
        m = matcher("AbTestCaseAssigned", "a == c*3");

        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("9", "2", "3");
        MyEvent e3 = fields("3", "5", "3");
        MyEvent e4 = fields("9", "5", "3");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                " >> ([[source:event.a]]1 == ([[source:event.c]]3 * [[source:constant]]3)9) << ");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "([[source:event.a]]9 == ([[source:event.c]]3 * [[source:constant]]3)9)");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == ([[source:event.c]]3 * [[source:constant]]3)9) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]9 == ([[source:event.c]]3 * [[source:constant]]3)9)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingExpressionWithDivision() throws Exception {
        m = matcher("AbTestCaseAssigned", "a == c/3");

        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("2", "2", "9");
        MyEvent e3 = fields("3", "5", "6");
        MyEvent e4 = fields("4", "5", "12");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "([[source:event.a]]1 == ([[source:event.c]]3 / [[source:constant]]3)1)");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                " >> ([[source:event.a]]2 == ([[source:event.c]]9 / [[source:constant]]3)3) << ");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.a]]3 == ([[source:event.c]]6 / [[source:constant]]3)2) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.a]]4 == ([[source:event.c]]12 / [[source:constant]]3)4)");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingContainingBrackets() throws Exception {
        m = matcher("AbTestCaseAssigned", "c == (b+1)*2");

        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("2", "2", "6");
        MyEvent e3 = fields("3", "5", "6");
        MyEvent e4 = fields("4", "5", "12");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                " >> ([[source:event.c]]3 == (([[source:event.b]]2 + [[source:constant]]1)3 * [[source:constant]]2)6) << ");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "([[source:event.c]]6 == (([[source:event.b]]2 + [[source:constant]]1)3 * [[source:constant]]2)6)");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                " >> ([[source:event.c]]6 == (([[source:event.b]]5 + [[source:constant]]1)6 * [[source:constant]]2)12) << ");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "([[source:event.c]]12 == (([[source:event.b]]5 + [[source:constant]]1)6 * [[source:constant]]2)12)");

        assertThat(m.matches(e1, null)).isFalse();
        assertThat(m.matches(e2, null)).isTrue();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchingWithComplexExpression() throws Exception {
        m = matcher("AbTestCaseAssigned", "a%(b+1) == c/3 || c > 10");

        MyEvent e1 = fields("1", "2", "3");
        MyEvent e2 = fields("2", "2", "9");
        MyEvent e3 = fields("3", "5", "6");
        MyEvent e4 = fields("4", "5", "12");

        assertThat(m.toDebugString(e1, null)).isEqualTo(
                "((([[source:event.a]]1 % ([[source:event.b]]2 + [[source:constant]]1)3)1 == ([[source:event.c]]3 / [[source:constant]]3)1) ||  >> ([[source:event.c]]3 > [[source:constant]]10) << )");
        assertThat(m.toDebugString(e2, null)).isEqualTo(
                "( >> (([[source:event.a]]2 % ([[source:event.b]]2 + [[source:constant]]1)3)2 == ([[source:event.c]]9 / [[source:constant]]3)3) <<  ||  >> ([[source:event.c]]9 > [[source:constant]]10) << )");
        assertThat(m.toDebugString(e3, null)).isEqualTo(
                "( >> (([[source:event.a]]3 % ([[source:event.b]]5 + [[source:constant]]1)6)3 == ([[source:event.c]]6 / [[source:constant]]3)2) <<  ||  >> ([[source:event.c]]6 > [[source:constant]]10) << )");
        assertThat(m.toDebugString(e4, null)).isEqualTo(
                "((([[source:event.a]]4 % ([[source:event.b]]5 + [[source:constant]]1)6)4 == ([[source:event.c]]12 / [[source:constant]]3)4) || ([[source:event.c]]12 > [[source:constant]]10))");

        assertThat(m.matches(e1, null)).isTrue();
        assertThat(m.matches(e2, null)).isFalse();
        assertThat(m.matches(e3, null)).isFalse();
        assertThat(m.matches(e4, null)).isTrue();
    }

    @Test
    public void testMatchWithSubStringFunction() throws Exception {
        MyEvent e = fields("abcdefgh");
        m = matcher("TestEvent", "substr(1,5,a) == 'bcde'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:substr(1, 5, 'abcdefgh')]]'bcde' == [[source:constant]]'bcde')");

        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithSubStringFunctionWhenError() throws Exception {
        MyEvent e = fields("abcdefgh");
        m = matcher("TestEvent", "substr(1, 'X', a) == 'bcde'");

        assertThat(m.toDebugString(e, null)).startsWith(
                " >> ([[source:substr(1, X, 'abcdefgh')]]'@ERROR on line: com.king.tratt.FunctionFactorySubstr");
        assertThat(m.toDebugString(e, null)).contains("message: java.lang.String cannot be cast to java.lang.Long");
        assertThat(m.toDebugString(e, null)).endsWith("; context: null' == [[source:constant]]'bcde') << ");

        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void testMatchWithSubStringFunction2() throws Exception {
        MyEvent e = fields("abcdefgh");
        m = matcher("TestEvent", "'bcde'==substr(1,5,a)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:constant]]'bcde' == [[source:substr(1, 5, 'abcdefgh')]]'bcde')");

        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithSubStringFunction3() throws Exception {
        MyEvent e = fields("abcdefgh");
        m = matcher("TestEvent", "'bcde'==substr(1,5,a) && a=='abcdefgh'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "(([[source:constant]]'bcde' == [[source:substr(1, 5, 'abcdefgh')]]'bcde') && ([[source:event.a]]abcdefgh == [[source:constant]]'abcdefgh'))");

        assertThat(m.matches(e, null)).isTrue();
    }


    @Test
    public void testMatchWithJsonFieldFunction() throws Exception {
        MyEvent e = fields("{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}");
        m = matcher("TestEvent", "jsonfield('o.p',a)=='panther'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:jsonfield('o.p', "
                        + "'{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}')]]'panther' == [[source:constant]]'panther')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunction2() throws Exception {
        MyEvent e = fields("{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}");
        m = matcher("TestEvent", "jsonfield('c',a)");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "[[source:jsonfield('c', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}')]]true");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunction3() throws Exception {
        MyEvent e = fields("{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}");
        m = matcher("TestEvent", "jsonfield('c',a)>1233");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                "([[source:jsonfield('c', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}')]]1234 > [[source:constant]]1233)");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldFunctionAndContext() throws Exception {
        String json = "{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}";
        MyEvent e = fields(json);
        context.set("$d","4321");
        context.set("$field","c");
        m = matcher("TestEvent", "jsonfield($field,a)<$d");

        assertThat(m.toDebugString(e, context)).isEqualTo(
                "([[source:jsonfield('c', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":1234}')]]1234 < [[source:context.$d]]4321)");
        assertThat(m.matches(e, context)).isTrue();
    }

    @Test
    public void testMatchWithJsonFieldWhenJsonPathIsIncorrect() throws Exception {
        MyEvent e = fields("{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}");
        m = matcher("TestEvent", "jsonfield('x',a)=='panther'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                " >> ([[source:jsonfield('x', '{\"a\": \"apa\",\"o\":{\"p\": \"panther\"},\"c\":true}')]]'"
                        + "[@ERROR incorrect json path: 'x']' == [[source:constant]]'panther') << ");
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void testMatchWithJsonFieldWhenJsonStringIsMalformed() throws Exception {
        MyEvent e = fields("{\"a\": ");
        m = matcher("TestEvent", "jsonfield('o.p',a)=='panther'");

        assertThat(m.toDebugString(e, null)).isEqualTo(
                " >> ([[source:jsonfield('o.p', '{\"a\": ')]]"
                        + "'[@ERROR malformed json string]' == [[source:constant]]'panther') << ");
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test(expected = IllegalStateException.class)
    public void testMatchWithJsonFieldWhenWrongNumberOfArguments() throws Exception {
        m = matcher("TestEvent", "jsonfield('o.p')=='panther'");
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithFields() throws Exception {
        MyEvent e = fields("abc", "123", "abc123");
        m = matcher("AbTestCaseAssigned", "c == concat(a,b)");

        assertThat(m.toDebugString(e, context)).isEqualTo(
                "([[source:event.c]]abc123 == [[source:concat('abc', '123')]]'abc123')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithFieldAndConstant() throws Exception {
        MyEvent e = fields("...", "123", "abc123");
        m = matcher("AbTestCaseAssigned", "c == concat('abc',b)");

        assertThat(m.toDebugString(e, context)).isEqualTo(
                "([[source:event.c]]abc123 == [[source:concat('abc', '123')]]'abc123')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithMoreThanTwoParams() throws Exception {
        MyEvent e = fields("...", "123", "abc123def");
        m = matcher("AbTestCaseAssigned", "c == concat('abc',b, 'def')");

        assertThat(m.toDebugString(e, context)).isEqualTo(
                "([[source:event.c]]abc123def == [[source:concat('abc', '123', 'def')]]'abc123def')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithComma() throws Exception {
        MyEvent e = fields("...", "123", "ab,g123");
        m = matcher("AbTestCaseAssigned", "c == concat('ab,g',b)");

        assertThat(m.toDebugString(e, context)).isEqualTo(
                "([[source:event.c]]ab,g123 == [[source:concat('ab,g', '123')]]'ab,g123')");
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canMatchWhenUsingConcatFunctionWithFieldAndVariable() throws Exception {
        MyEvent e = fields("...", "123", "1234321");
        context.set("$d", "4321");
        m = matcher("AbTestCaseAssigned", "c == concat(b, $d)");

        assertThat(m.toDebugString(e, context)).isEqualTo(
                "([[source:event.c]]1234321 == [[source:concat('123', '4321')]]'1234321')");
        assertThat(m.matches(e, context)).isTrue();
    }

    @Test
    public void canMatchWhenUsingSplitFunctionWithFields() throws Exception {
        MyEvent e = fields("AAA,BBB,,DDD", "...", "...");
        m = matcher("AbTestCaseAssigned", "'DDD' == split(a, ',', 3)");

        String expected = "([[source:constant]]'DDD' == [[source:split('AAA,BBB,,DDD', ',', 3)]]'DDD')";
        assertThat(m.toDebugString(e, null)).isEqualTo(expected);
        assertThat(m.matches(e, null)).isTrue();
    }

    @Test
    public void canFailWhenUsingSplitFunctionWithFieldsAndIndexIsOutOfBounce() throws Exception {
        MyEvent e = fields("AAA,BBB", "...", "...");
        m = matcher("AbTestCaseAssigned", "'AAA' == split(a, ',', 2)");

        String expected = " >> ([[source:constant]]'AAA' == [[source:split('AAA,BBB', ',', 2)]]"
                + "'[@ERROR array index '2' out of bounce]') << ";
        assertThat(m.toDebugString(e, null)).isEqualTo(expected);
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void canCompareValuesWithEmptyString() throws Exception {
        m = matcher("AbTestCaseAssigned", "a == ''");

        MyEvent e = fields("AAA", "...", "...");
        String expected = " >> ([[source:event.a]]AAA == [[source:constant]]'') << ";
        String actual = m.toDebugString(e, null);
        assertThat(actual).isEqualTo(expected);
        assertThat(m.matches(e, null)).isFalse();
    }

    @Test
    public void canCheckSufficientContextWhenCalculatingSum() throws Exception {
        MyEvent e = fields("AAA", "6", "...");
        context.set("ctx", "1");
        m = matcher("AbTestCaseAssigned", "ctx + 5 == b");

        assertThat(m.hasSufficientContext(e, new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> (([[source:context.ctx]][Insufficient Context!] + [[source:constant]]5)[Insufficient Context!] == [[source:event.b]]6) << ");

        assertThat(m.hasSufficientContext(e, context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:context.ctx]]1 + [[source:constant]]5)6 == [[source:event.b]]6)");
    }

    @Test
    public void canCheckSufficientContextWhenSubtracting() throws Exception {
        MyEvent e = fields("AAA", "4", "...");
        context.set("ctx", "1");
        m = matcher("AbTestCaseAssigned", "5 - ctx == b");

        assertThat(m.hasSufficientContext(e, new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> (([[source:constant]]5 - [[source:context.ctx]][Insufficient Context!])[Insufficient Context!] == [[source:event.b]]4) << ");

        assertThat(m.hasSufficientContext(e, context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 - [[source:context.ctx]]1)4 == [[source:event.b]]4)");
    }

    @Test
    public void canCheckSufficientContextWhenDividing() throws Exception {
        MyEvent e = fields("AAA", "5", "...");
        context.set("ctx", "1");
        m = matcher("AbTestCaseAssigned", "5 / ctx == b");

        assertThat(m.hasSufficientContext(e, new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> (([[source:constant]]5 / [[source:context.ctx]][Insufficient Context!])[Insufficient Context!] == [[source:event.b]]5) << ");

        assertThat(m.hasSufficientContext(e, context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 / [[source:context.ctx]]1)5 == [[source:event.b]]5)");
    }

    @Test
    public void canCheckSufficientContextWhenCalculatingModulus() throws Exception {
        MyEvent e = fields("AAA", "1", "...");
        context.set("ctx", "2");
        m = matcher("AbTestCaseAssigned", "5 % ctx == b");

        assertThat(m.hasSufficientContext(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> (([[source:constant]]5 % [[source:context.ctx]][Insufficient Context!])[Insufficient Context!] == [[source:event.b]]1) << ");
        assertThat(m.matches(e, new ContextImp())).isFalse();

        assertThat(m.hasSufficientContext(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 % [[source:context.ctx]]2)1 == [[source:event.b]]1)");
        assertThat(m.matches(e, context)).isTrue();
    }

    @Test
    public void canCheckSufficientContextWhenMultiplying() throws Exception {
        MyEvent e = fields("AAA", "15", "...");
        context.set("ctx", "3");
        m = matcher("AbTestCaseAssigned", "5 * ctx == b");

        assertThat(m.hasSufficientContext(e, new ContextImp())).isFalse();
        assertThat(m.matches(e, new ContextImp())).isFalse();
        assertThat(m.toDebugString(e, new ContextImp())).isEqualTo(
                " >> (([[source:constant]]5 * [[source:context.ctx]][Insufficient Context!])[Insufficient Context!] == [[source:event.b]]15) << ");

        assertThat(m.hasSufficientContext(e, context)).isTrue();
        assertThat(m.matches(e, context)).isTrue();
        assertThat(m.toDebugString(e, context)).isEqualTo(
                "(([[source:constant]]5 * [[source:context.ctx]]3)15 == [[source:event.b]]15)");
    }

}
