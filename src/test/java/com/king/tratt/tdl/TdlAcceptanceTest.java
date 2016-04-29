/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.tdl;

import static com.king.tratt.tdl.CheckPointBuilder.forEvent;
import static com.king.tratt.tdl.Sequence.Type.CONTAINER;
import static com.king.tratt.tdl.Sequence.Type.UNWANTED;
import static com.king.tratt.tdl.SequenceBuilder.ofType;
import static com.king.tratt.tdl.Tdl.newBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.king.tratt.tdl.Sequence.Type;

public class TdlAcceptanceTest {

    private Tdl tdl;

    static Tdl fromPath(String name) {
        return Tdl.fromPath("classpath:com/king/tratt/tdl/" + name);
    }

    @Test
    public void canMergeTdlFilesWithBuilder() throws Exception {
        Tdl tdlA = fromPath("merge-a.tdl");
        Tdl tdlB = fromPath("merge-b.tdl");
        Tdl tdlC = fromPath("merge-c.tdl");
        Tdl mergedAB = fromPath("merge-result-ab.tdl");
        Tdl mergedABC = fromPath("merge-result-abc.tdl");

        Tdl tdlAB = newBuilder().addTdls(tdlA, tdlB).build();
        assertThat(tdlAB).isEqualTo(mergedAB);

        Tdl tdlABC = Tdl.newBuilder().addTdls(tdlAB, tdlC).build();
        assertThat(tdlABC).isEqualTo(mergedABC);

        Tdl tdlABC2 = Tdl.merge(tdlA, tdlB, tdlC);
        assertThat(tdlABC2).isEqualTo(mergedABC);
    }

    @Test
    public void canMergeTdlFiles() throws Exception {
        Tdl tdlA = fromPath("merge-a.tdl");
        Tdl tdlB = fromPath("merge-b.tdl");
        Tdl tdlC = fromPath("merge-c.tdl");
        Tdl mergedAB = fromPath("merge-result-ab.tdl");
        Tdl mergedABC = fromPath("merge-result-abc.tdl");

        Tdl tdlAB = Tdl.merge(tdlA, tdlB);
        assertThat(tdlAB).isEqualTo(mergedAB);

        Tdl tdlABC = Tdl.merge(tdlAB, tdlC);
        assertThat(tdlABC).isEqualTo(mergedABC);

        Tdl tdlABC2 = Tdl.merge(tdlA, tdlB, tdlC);
        assertThat(tdlABC2).isEqualTo(mergedABC);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMergingNullTdl() throws Exception {
        Tdl.merge(null);
    }

    @Test
    public void canGetSequenceByNameFromTdl() throws Exception {
        // given
        tdl = fromPath("two-sequences.tdl");
        String seq1Name = "seq-1";
        String seq2Name = "seq-2";

        // then
        assertThat(tdl.containsSequence(seq1Name)).isTrue();
        assertThat(tdl.containsSequence(seq2Name)).isTrue();
        assertThat(tdl.containsSequence("unknown")).isFalse();
        assertThat(tdl.getSequence(seq1Name).getName()).isEqualTo(seq1Name);
        assertThat(tdl.getSequence("seq-2").getName()).isEqualTo("seq-2");

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGettingSequenceByNameAndNoSuchSequence() throws Exception {
        // given
        tdl = Tdl.fromPath("two-sequences.tdl");

        // then
        tdl.getSequence("unknown");
    }

    @Test
    public void canGetDefaultValuesWhenReadingTdlFromFile() throws Exception {
        tdl = fromPath("GoodEmpty.tdl");

        Sequence seq0 = tdl.getSequences().get(0);
        CheckPoint checkPoint = seq0.getCheckPoints().get(0);

        assertThat(tdl.getComment()).isEmpty();
        assertThat(tdl.getVariables()).isEmpty();

        assertThat(seq0.getSequenceMaxTime()).isEqualToIgnoringCase("PT15M");
        assertThat(seq0.getName()).isNull();

        assertThat(checkPoint.getEventType()).isNull();
        assertThat(checkPoint.getMatch()).isEmpty();
        assertThat(checkPoint.getSet()).isEmpty();
        assertThat(checkPoint.getValidate()).isEmpty();
    }

    @Test
    public void canGetDefaultValuesWhenCreatingTdlProgramatically() throws Exception {
        tdl = Tdl.newBuilder()
                .addSequence(ofType(UNWANTED).name("seq0"))
                .addSequence(ofType(CONTAINER)
                        .name("seq1")
                        .withCheckPoint(forEvent("ItemTransaction4")))
                .build();

        Sequence seq0 = tdl.getSequences().get(0);
        Sequence seq1 = tdl.getSequences().get(1);
        CheckPoint checkPoint = seq1.getCheckPoints().get(0);

        assertThat(tdl.getComment()).isEmpty();
        assertThat(tdl.getVariables()).isEmpty();

        assertThat(seq0.getSequenceMaxTime()).isEqualToIgnoringCase("PT15M");
        assertThat(seq0.getName()).isEqualTo("seq0");

        assertThat(seq1.getSequenceMaxTime()).isEqualToIgnoringCase("PT15M");
        assertThat(seq1.getName()).isEqualTo("seq1");

        assertThat(checkPoint.getEventType()).isEqualTo("ItemTransaction4");
        assertThat(checkPoint.getMatch()).isEmpty();
        assertThat(checkPoint.getSet()).isEmpty();
        assertThat(checkPoint.getValidate()).isEmpty();
    }

    @Test
    public void canGetCorrectSequenceValuesFromTdl() throws Exception {
        tdl = fromPath("GoodWith10SecondsTimeout.tdl");
        Sequence seq = tdl.getSequences().get(0);
        CheckPoint checkPoint = seq.getCheckPoints().get(0);

        assertThat(tdl.getComment()).isEqualTo("Validation of EventA");
        assertThat(tdl.getVariables()).containsOnly("varA=valA");

        assertThat(seq.getName()).isEqualToIgnoringCase("EventA-test");
        assertThat(seq.getSequenceMaxTime()).isEqualToIgnoringCase("pt10s");
        assertThat(seq.getType()).isEqualTo(UNWANTED);

        assertThat(checkPoint.getEventType()).isEqualToIgnoringCase("EventA");
        assertThat(checkPoint.getMatch()).isEmpty();
        assertThat(checkPoint.getSet()).isEmpty();
        assertThat(checkPoint.getValidate()).isEqualTo("field3==$varA");
    }

    @Test
    public void canSerializeWhenOnlySequencesInTdl() throws Exception {
        tdl = fromPath("OnlySequences.tdl");
        String serialized = tdl.toString();

        assertThat(serialized).containsIgnoringCase("\"type\": \"UNWANTED\"");
        assertThat(serialized).containsIgnoringCase("\"validate\": \"field3==$varA\"");
    }

    @Test
    public void canCreateTdlProgramaticallyWithTwoSequences() throws Exception {
        tdl = Tdl.newBuilder()
                .addVariable("var1", "val1")
                .addVariable("var2", 1)
                .addVariable("var3", 2L)
                .setComment("my-comment")
                .addSequence(ofType(CONTAINER)
                        .maxTime(15, TimeUnit.MINUTES)
                        .name("my-sequence")
                        .withCheckPoint(forEvent("ExternalStoreTransactionBegin")
                                .match("coreUserId == $coreUserId")
                                .validate("fieldA == aValue && fieldB == anotherValue")
                                .set("name1=value1", "name2=value2")))
                .addSequence(ofType(UNWANTED)
                        .name("seq-name")
                        .withCheckPoint(forEvent("ExternalStoreTransactionDone")
                                .match("coreUserId == $coreUserId")
                                .validate("fieldA == aValue && fieldB == anotherValue")
                                .set("name1=value1", "name2=value2")))
                .build();

        assertThat(tdl.getSequences()).hasSize(2);
        assertThat(tdl.getVariables()).containsOnly("var1=val1", "var2=1", "var3=2");
        assertThat(tdl.areAllVariablesSet()).isTrue();
        assertThat(tdl.getComment()).isEqualTo("my-comment");

        Sequence sequence0 = tdl.getSequences().get(0);
        assertThat(sequence0.getName()).isEqualTo("my-sequence");
        assertThat(sequence0.getType()).isEqualTo(Sequence.Type.CONTAINER);
        assertThat(sequence0.getSequenceMaxTime()).isEqualToIgnoringCase("pt15M");
        assertThat(sequence0.getCheckPoints()).hasSize(1);

        CheckPoint checkPoint0 = sequence0.getCheckPoints().get(0);
        assertThat(checkPoint0.getMatch()).isEqualTo("coreUserId == $coreUserId");
        assertThat(checkPoint0.getEventType()).isEqualTo("ExternalStoreTransactionBegin");
        assertThat(checkPoint0.getSet()).containsOnly("name1=value1", "name2=value2");
        assertThat(checkPoint0.getValidate())
                .isEqualTo("fieldA == aValue && fieldB == anotherValue");
    }

    @Test
    public void canGetExistingVariableFromTdl() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        String value = tdl.getVariable("varA");

        // then
        assertThat(value).isEqualTo("valA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGettingUnexistingVariable() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        tdl.getVariable("unknown");
    }

    @Test
    public void canDetectIfVariableIsAvailableOrNot() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        assertThat(tdl.containsVariable("unknown")).isFalse();
        assertThat(tdl.containsVariable("varA")).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenGettingVariableAndVariableNameIsNull() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        tdl.getVariable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGettingVariableAndVariableNameIsUnknown() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        assertThat(tdl.containsVariable("dummy")).isFalse();
        tdl.getVariable("dummy");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenAskingIfVariableExistsAndVariableNameIsNull() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        tdl.containsVariable(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenCheckinigIfTdlContainsNullSequence() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        tdl.containsSequence(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenTryingToGetNullSequence() throws Exception {
        // given
        tdl = fromPath("Good.tdl");

        // when
        tdl.getSequence(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenInvalidJson() throws Exception {
        fromPath("invalid-json-.tdl");
    }

    @Test
    public void canDetectWhenAVariableIsNotSet() throws Exception {
        tdl = fromPath("EmptyVariables.tdl");

        assertThat(tdl.areAllVariablesSet()).isFalse();
    }

    @Test
    public void canAddExpectedVariableWhenModifingExistingTdlWithDuplicatedVariables()
            throws Exception {
        tdl = Tdl.newBuilder()
                .addVariable("var1", "AA")
                .addVariable("var2", "BB")
                .addTdls(fromPath("DuplicatedVariables.tdl"))
                .build();

        assertThat(tdl.getVariables()).containsOnly("var1=AA", "var2=BB");
    }

    @Test
    public void canAddExpectedVariableWhenModifingExistingTdl() throws Exception {
        tdl = Tdl.newBuilder()
                .addVariable("var1", "AA")
                .addVariable("var2", "BB")
                .addTdls(fromPath("EmptyVariables.tdl"))
                .build();

        assertThat(tdl.getVariables()).containsOnly("var1=AA", "var2=BB");
    }

    @Test
    public void canRetainValuesFromExistingTdl() throws Exception {

        tdl = Tdl.newBuilder()
                .addTdls(fromPath("Good.tdl")).build();

        assertThat(tdl.getSequences()).hasSize(1);
        assertThat(tdl.getVariables()).containsOnly("varA=valA");
        assertThat(tdl.getComment()).isEqualTo("Validation of EventA");

        Sequence sequence = tdl.getSequences().get(0);
        assertThat(sequence.getName()).isEqualTo("EventA-test");
        assertThat(sequence.getSequenceMaxTime()).isEqualToIgnoringCase("PT15M");
        assertThat(sequence.getType()).isEqualTo(UNWANTED);

        CheckPoint checkPoint = sequence.getCheckPoints().get(0);
        assertThat(checkPoint.getEventType()).isEqualTo("EventA");
        assertThat(checkPoint.getMatch()).isEmpty();
        assertThat(checkPoint.getSet()).isEmpty();
        assertThat(checkPoint.getValidate()).isEqualTo("field3==$varA");
    }

    @Test
    public void canModifyExistingTdl() throws Exception {

        tdl = Tdl.newBuilder()
                .addVariable("name", "value")
                .setComment("new-comment")
                .addTdls(fromPath("Good.tdl"))
                .addSequence(ofType(CONTAINER)
                        .name("seq-name")
                        .withCheckPoint(forEvent("ItemTransaction4")))
                .build();

        assertThat(tdl.getSequences()).hasSize(2);
        assertThat(tdl.getVariables()).containsOnly("name=value", "varA=valA");
        assertThat(tdl.getComment()).isEqualTo("new-comment");

        List<Type> seqTypes = new ArrayList<>();
        for (Sequence seq : tdl.getSequences()) {
            seqTypes.add(seq.getType());
        }
        assertThat(seqTypes).containsOnly(CONTAINER, UNWANTED);
    }

    @Test
    public void canParseTdlFromBytes() throws Exception {
        URI uri = ClassLoader.getSystemResource("com/king/tratt/tdl/Good.tdl").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(uri));

        verifyGoodTdl(Tdl.fromBytes(bytes));
    }

    @Test
    public void canParseTdlFromJson() throws Exception {
        URI uri = ClassLoader.getSystemResource("com/king/tratt/tdl/Good.tdl").toURI();
        byte[] bytes = Files.readAllBytes(Paths.get(uri));
        String json = new String(bytes, "UTF-8");

        verifyGoodTdl(Tdl.fromJson(json));
    }

    @Test
    public void canParseTdlFromClasspath() throws Exception {
        verifyGoodTdl(fromPath("Good.tdl"));
    }

    @Test
    public void canParseTdlFromPath() throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource("com/king/tratt/tdl/Good.tdl").toURI());
        verifyGoodTdl(Tdl.fromPath(path));
    }

    @Test
    public void canParseTdlFromFilepath() throws Exception {
        String fileUrl = ClassLoader.getSystemResource("com/king/tratt/tdl/Good.tdl")
                .toExternalForm();
        assertThat(fileUrl).startsWith("file:");

        verifyGoodTdl(Tdl.fromPath(fileUrl));
    }

    @Test
    public void canParseTdlFromLocalFilesystem() throws Exception {
        File fileUrl = new File(
                ClassLoader.getSystemResource("com/king/tratt/tdl/Good.tdl").toURI());
        verifyGoodTdl(Tdl.fromPath(fileUrl.getPath()));
    }

    private void verifyGoodTdl(Tdl tdl) {
        assertThat(tdl.getComment()).isEqualTo("Validation of EventA");
        assertThat(tdl.getVariables()).containsOnly("varA=valA");
        assertThat(tdl.getSequences()).hasSize(1);

        Sequence sequence = tdl.getSequences().get(0);
        assertThat(sequence.getName()).isEqualTo("EventA-test");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPassingNullArgumentToMethod_fromReader() throws Exception {
        Tdl.fromReader(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPassingNullArgumentToMethod_fromPath() throws Exception {
        Tdl.fromPath((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPassingNullArgumentToMethod_fromPath2() throws Exception {
        Tdl.fromPath((Path) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPassingNullArgumentToMethod_fromBytes() throws Exception {
        Tdl.fromBytes(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPassingNullArgumentToMethod_fromJson() throws Exception {
        Tdl.fromJson(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenPassingIllegalArgumentToMethod_fromUrl() throws Exception {
        Tdl.fromUrl(new URL("http://localhost"));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPassingNullArgumentToMethod_fromUrl() throws Exception {
        Tdl.fromUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenPassingEmptyStringToMethod_fromPath() throws Exception {
        Tdl.fromPath("");
    }

    @Test
    public void canParseSequenceCorrectly() throws Exception {
        tdl = fromPath("Good.tdl");
        assertThat(tdl.getSequences()).hasSize(1);

        Sequence seq = tdl.getSequences().get(0);
        assertThat(seq.getCheckPoints()).hasSize(1);
        assertThat(seq.getName()).isEqualTo("EventA-test");
        assertThat(seq.getSequenceMaxTime()).isEqualToIgnoringCase("pt15m");
        assertThat(seq.getType()).isEqualTo(UNWANTED);
    }

    @Test
    public void canParseCheckPointCorrectly() throws Exception {
        tdl = fromPath("Good.tdl");
        assertThat(tdl.getSequences()).hasSize(1);

        CheckPoint checkPoint = tdl.getSequences().get(0).getCheckPoints().get(0);
        assertThat(checkPoint.getEventType()).isEqualTo("EventA");
        assertThat(checkPoint.getMatch()).isEmpty();
        assertThat(checkPoint.getSet()).isEmpty();
        assertThat(checkPoint.getValidate()).isEqualTo("field3==$varA");
    }

    @Test
    public void canDetectIfVariableValueIsSetOrNot() throws Exception {
        assertThat(Tdl.hasNoValue("var1=A")).isFalse();
        assertThat(Tdl.hasNoValue("var1=")).isTrue();
        assertThat(Tdl.hasNoValue("var1= ")).isTrue();
        assertThat(Tdl.hasNoValue("=")).isTrue();
        assertThat(Tdl.hasNoValue("var1")).isTrue();
    }
}
