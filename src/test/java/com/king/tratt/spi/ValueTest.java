package com.king.tratt.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.king.tratt.spi.test.imp.TestEvent;

public class ValueTest {
    private static final TestEvent EVENT = null;
    private static final Context CONTEXT = null;
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void canGetBooleanValue() throws Exception {
        // given
        Value value = new Value() {

            @Override
            public String toDebugString(Event e, Context context) {
                return "validation-debug-string";
            }

            @Override
            protected Boolean getImp(Event e, Context context) {
                return false;
            }

            @Override
            public String toString() {
                return "toString text";
            }
        };

        // then
        assertThat(value.asString(EVENT, CONTEXT)).isEqualTo("false");
        assertThat(value.get(EVENT, CONTEXT)).isEqualTo(false);
        assertThat(value.hasSufficientContext(CONTEXT)).isTrue();
        assertThat(value.toDebugString(EVENT, CONTEXT)).isEqualTo("validation-debug-string");
        assertThat(value.toString()).isEqualTo("toString text");
    }

    private static class ValueForTesting extends Value {

        @SafeVarargs
        public ValueForTesting(ValueForTesting... values) {
            super(values);
        }

        public ValueForTesting(List<ValueForTesting> awares) {
            super(awares);
        }

        @Override
        public String toDebugString(Event e, Context context) {
            return null;
        }

        @Override
        protected Object getImp(Event e, Context context) {
            return null;
        }

        @Override
        public String toString() {
            return "ValueForTesting";
        }

    }
    @Test
    public void shouldThrowWhenUnsupportedReturnType() throws Exception {
        // given
        expected.expect(UnsupportedReturnTypeException.class);
        expected.expectMessage("Method 'getImp' has unsupported return type.");

        // when
        new ValueForTesting() {

            @Override
            public String toDebugString(Event e, Context context) {
                return "error-text";
            }

            @Override
            // With Unsupported return value 'File'
            protected File getImp(Event e, Context context) {
                return null;
            }
        };
    }
    
    @Test
    public void shouldThrowWhenGetImpThrows() throws Exception {
        // given
        expected.expect(IllegalStateException.class);
        expected.expectMessage("Unexpected crash! See underlying exceptions for more info.");
        expected.expectMessage("value: ValueForTesting; event: null; context: null");
        
        // when
        Value value = new ValueForTesting() {
            @Override
            protected Object getImp(Event e, Context context) {
                throw new RuntimeException("testing");
            }
        };
        
        // then
        value.get(EVENT, CONTEXT);
    }

    @Test
    public void checkIfAwaresHasSufficiantContext() throws Exception {
        Value value = new ValueForTesting(new ValueForTesting(), new ValueForTesting() {
            @Override
            public boolean hasSufficientContext(Context context) {
                return false;
            }
        });
        assertThat(value.hasSufficientContext(CONTEXT)).isFalse();
    }

    @Test
    public void checkAsString() throws Exception {
        Value value = new ValueForTesting() {
            @Override
            protected Object getImp(Event e, Context context) {
                return 5L;
            }
        };
        assertThat(value.asString(EVENT, CONTEXT)).isEqualTo("5");
    }

    @Test
    public void shouldThrowWhenConstructorIsFedWithNullList() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage("'awares' must not be null!");
        new ValueForTesting((List<ValueForTesting>) null);
    }

    @Test
    public void shouldThrowWhenConstructorIsFedWithNullVarArg() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage("'awares' must not be null!");
        ValueForTesting[] arg = null;
        new ValueForTesting(arg);
    }

    @Test
    public void shouldThrowWhenConstructorIsFedWithAtleastOneNullElements() throws Exception {
        expected.expect(NullPointerException.class);
        expected.expectMessage("'awares' must not contain any null elements!");
        List<ValueForTesting> list = Arrays.asList(new ValueForTesting(), null);
        new ValueForTesting(list);
    }

}
