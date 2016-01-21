package com.king.tratt.tdl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.king.tratt.tdl.Sequence.Type;

public class TdlBuilderTest {

    TdlBuilder builder = Tdl.newBuilder();

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenDurationIsNegative() throws Exception {
        builder.setSequencesMaxTime(-1, TimeUnit.SECONDS);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenTimeUnitIsNull() throws Exception {
        builder.setSequencesMaxTime(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_setSequncesType() throws Exception {
        builder.setSequencesType(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFirstArgumentIsNull_addVariableString() throws Exception {
        builder.addVariable(null, "a");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFirstArgumentIsNull_addVariableInt() throws Exception {
        builder.addVariable(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFirstArgumentIsNull_addVariableLong() throws Exception {
        builder.addVariable(null, 1L);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenSecondArgumentIsNull_addVariableString() throws Exception {
        builder.addVariable("", (String) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenSecondArgumentIsNull_addVariableInteger() throws Exception {
        builder.addVariable("", (Integer) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenSecondArgumentIsNull_addVariableLong() throws Exception {
        builder.addVariable("", (Long) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_addSequence() throws Exception {
        builder.addSequence(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSequenceBuilderNameIsNull() throws Exception {
        builder.addSequence(SequenceBuilder.ofType(Type.CONTAINER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSequenceBuilderNameIsEmpty() throws Exception {
        builder.addSequence(SequenceBuilder.ofType(Type.CONTAINER).name(""));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_addSequenceInvariants() throws Exception {
        builder.addSequenceInvariants(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenNoVarArgIsGiven_addCoreUserIdFilter() throws Exception {
        builder.addCoreUserIdFilter();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_addCoreUserIdFilter() throws Exception {
        builder.addCoreUserIdFilter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenOneVarArgIsEmptyString_addSequenceInvariants() throws Exception {
        builder.addSequenceInvariants("s", "");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenOneVarArgIsNull_addSequenceInvariants() throws Exception {
        builder.addSequenceInvariants(null, "s");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_useTdl() throws Exception {
        builder.useTdl(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_addTdls1() throws Exception {
        builder.addTdls((List<Tdl>) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenVarArgIsNull_addTdls1() throws Exception {
        builder.addTdls(builder.build(), (Tdl[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenOneOfRestArgsIsNull_addTdls1() throws Exception {
        builder.addTdls(builder.build(), (Tdl) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenArgumentIsEmpty_useTdl() throws Exception {
        builder.addTdls(new ArrayList<Tdl>());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_match() throws Exception {
        builder.addMatch(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenArgumentIsNull_setComment() throws Exception {
        builder.setComment(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenAddingEmptyVariableNameWithStringName() throws Exception {
        builder.addVariable("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenAddingEmptyVariableNameWithIntegerValue() throws Exception {
        builder.addVariable("", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenAddingEmptyVariableNameWithLongValue() throws Exception {
        builder.addVariable("", 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenAddingEmptyVariableValue() throws Exception {
        builder.addVariable("name", "");
    }

}
