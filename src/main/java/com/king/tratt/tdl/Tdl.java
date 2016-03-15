/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt.tdl;

import static com.king.tratt.tdl.Util.concat;
import static com.king.tratt.tdl.Util.nullArgumentError;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.king.tratt.tdl.Sequence.Type;
import com.king.tratt.tdl.TdlInternal.SequenceInternal;


/**
 * Created by magnus.ramstedt on 29/10/14.
 */
public final class Tdl {

    private static final ClassLoader CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private static final String CLASSPATH_PROTOCOL = "classpath:";
    private static final String FILE_PROTOCOL = "file:";
    private static final String AND_OPERATOR = " && ";
    private static final Gson GSON = new Gson();

    private final TdlInternal tdlInternal;
    private final Map<String, String> variables = new LinkedHashMap<>();


    public static TdlBuilder newBuilder() {
        return new TdlBuilder();
    }

    public static Tdl fromJson(String json) {
        if (json == null) {
            throw nullArgumentError("json");
        }
        return new Tdl(GSON.fromJson(json, TdlInternal.class));
    }

    public static Tdl fromBytes(byte[] bytes) {
        if (bytes == null) {
            throw nullArgumentError("bytes");
        }
        try {
            return fromJson(new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Tdl fromPath(Path path) {
        return fromPath(path.toString());
    }

    public static Tdl fromPath(String prefixedPath) {
        if (prefixedPath == null) {
            throw nullArgumentError("prefixedPath");
        }
        URL url;
        try {
            if (prefixedPath.startsWith(CLASSPATH_PROTOCOL)) {
                String path = prefixedPath.substring(CLASSPATH_PROTOCOL.length());
                url = CLASS_LOADER.getResource(path);
            } else if (prefixedPath.startsWith(FILE_PROTOCOL)) {
                url = new URL(prefixedPath);
            } else {
                url = Paths.get(prefixedPath).toUri().toURL();
            }
            return fromUrl(url);
        } catch (IOException | RuntimeException e) {
            String message = "Cannot read file from path: '%s'";
            throw new IllegalArgumentException(String.format(message, prefixedPath), e);
        }
    }

    public static Tdl fromUrl(URL url) {
        if (url == null) {
            throw nullArgumentError("url");
        }
        try {
            return fromReader(new InputStreamReader(url.openStream(), "UTF-8"));
        } catch (IOException e) {
            String message = "Cannot parse tdl-file from URL '%s'";
            throw new IllegalArgumentException(String.format(message, url));
        }
    }

    public static Tdl fromReader(Reader reader) {
        if (reader == null) {
            throw nullArgumentError("reader");
        }
        TdlInternal tdl = GSON.fromJson(reader, TdlInternal.class);
        return new Tdl(tdl);
    }

    public static Tdl merge(Tdl first, Tdl... rest) {
        return merge(concat(first, rest));
    }

    public static Tdl merge(List<Tdl> tdls) {
        return newBuilder().addTdls(tdls).build();
    }

    private Tdl(TdlInternal tdl) {
        tdlInternal = tdl;
        variables.putAll(toMap(tdl.variables));
    }

    private Map<String, String> toMap(Collection<String> variables) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String keyValuePair : variables) {
            String[] splitted = keyValuePair.split("=", 2);
            String key = splitted[0].trim();
            String value = ""; // use as fallback if variable is empty in TDL.
            if (splitted.length == 2) {
                value = splitted[1];
            }
            result.put(key, value);
        }
        return result;
    }


    Tdl(TdlBuilder builder) {
        tdlInternal = new TdlInternal();
        Map<String, SequenceBuilder> tempSequenceBuilders = new LinkedHashMap<>();
        prepareData(builder, tempSequenceBuilders);
        populateTdlInternalWithData(builder, tempSequenceBuilders);
    }

    private void prepareData(TdlBuilder builder, Map<String, SequenceBuilder> tempSequenceBuilders) {
        if (builder.useTdl != null) {
            extractData(builder.useTdl, tempSequenceBuilders);
        } else if (!builder.addedTdls.isEmpty()) {
            for (Tdl tdl : builder.addedTdls) {
                extractData(tdl, tempSequenceBuilders);
            }
        }
        variables.putAll(builder.variables);
        mergeSequenceBuilders(tempSequenceBuilders, builder.sequenceBuilders.values());
    }

    private void populateTdlInternalWithData(TdlBuilder builder, Map<String, SequenceBuilder> tempSequenceBuilders) {
        if (builder.comment != null) {
            tdlInternal.comment = builder.comment;
        }
        tdlInternal.addSequenceInvariants(builder.sequenceInvariants);
        tdlInternal.variables = fromMap(variables);

        for (SequenceBuilder sequenceBuilder : tempSequenceBuilders.values()) {
            if (!builder.matchExpressions.isEmpty()) {
                prependMatchExpressions(sequenceBuilder, builder.matchExpressions);
            }
            Type sequencesTypeCopy = builder.sequencesType;
            if (sequencesTypeCopy != null) {
                sequenceBuilder.type(sequencesTypeCopy);
            }
            Long sequencesMaxTimeMillisCopy = builder.sequencesMaxTimeMillis;
            if (sequencesMaxTimeMillisCopy != null) {
                sequenceBuilder.maxTime(sequencesMaxTimeMillisCopy, MILLISECONDS);
            }
            tdlInternal.sequences.add(sequenceBuilder.build().seqInternal);
        }
    }

    private Set<String> fromMap(Map<String, String> variables) {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result.add(entry.getKey() + "=" + entry.getValue());
        }
        return result;
    }

    private void extractData(Tdl tdl, Map<String, SequenceBuilder> tempSequenceBuilders) {
        tdlInternal.comment = tdl.getComment();
        tdlInternal.addSequenceInvariants(tdl.getSequenceInvariants());
        variables.putAll(tdl.variables);
        mergeSequences(tempSequenceBuilders, tdl.getSequences());
    }

    private void mergeSequences(Map<String, SequenceBuilder> temp, List<Sequence> sequences) {
        List<SequenceBuilder> result = new ArrayList<>();
        for (Sequence sequence : sequences) {
            result.add(SequenceBuilder.copyOf(sequence));
        }
        mergeSequenceBuilders(temp, result);
    }

    private void mergeSequenceBuilders(Map<String, SequenceBuilder> temp, Collection<SequenceBuilder> sbs) {
        for (SequenceBuilder sb : sbs) {
            if (temp.containsKey(sb.name)) {
                SequenceBuilder sequenceBuilder = temp.get(sb.name);
                sequenceBuilder.merge(sb);
            } else {
                temp.put(sb.name, sb);
            }
        }
    }

    private static void prependMatchExpressions(SequenceBuilder builder, List<String> matchExpressions) {
        StringBuilder newMatch = new StringBuilder();
        String operator = "";
        for (String match : matchExpressions) {
            newMatch.append(operator).append(match);
            operator = AND_OPERATOR;
        }
        if (!builder.match.isEmpty()) {
            newMatch.append(AND_OPERATOR).append(builder.match);
        }
        builder.match = newMatch.toString();
    }

    public String getComment() {
        return tdlInternal.comment;
    }

    public List<String> getSequenceInvariants() {
        return tdlInternal.getSequenceInvariants();
    }

    public List<String> getVariables() {
        return new ArrayList<>(tdlInternal.variables);
    }

    /**
     * @param name of sequence, case sensitive.
     * @return true if {@link Sequence} exists, otherwise false.
     */
    public boolean containsSequence(String name) {
        if (name == null) {
            throw nullArgumentError("name");
        }
        for (Sequence seq : getSequences()) {
            if (name.equals(seq.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param name of sequence, case sensitive.
     * @return the wanted {@link Sequence}.
     */
    public Sequence getSequence(String name) {
        if (name == null) {
            throw nullArgumentError("name");
        }
        for (Sequence seq : getSequences()) {
            if (name.equals(seq.getName())) {
                return seq;
            }
        }
        String message = "Sequence with name '%s' not found.";
        throw new IllegalArgumentException(format(message, name));
    }

    public List<Sequence> getSequences() {
        List<Sequence> sequences = new ArrayList<>();
        for (SequenceInternal seqInternal : tdlInternal.sequences) {
            sequences.add(new Sequence(seqInternal));
        }
        return sequences;
    }

    @Override
    public String toString() {
        return asJsonPrettyPrinted();
    }

    public String asJsonPrettyPrinted() {
        return Util.asJsonPrettyPrinted(tdlInternal);
    }

    public String asJson() {
        return Util.asJson(tdlInternal);
    }

    public boolean areAllVariablesSet() {
        for (String variable : tdlInternal.variables) {
            if (hasNoValue(variable)) {
                return false;
            }
        }
        return true;
    }

    static boolean hasNoValue(String variable) {
        return !variable.matches("\\S+=\\S+");
    }

    public String getVariable(String name) {
        if (name == null) {
            throw nullArgumentError("name");
        }

        if (containsVariable(name)) {
            return variables.get(name);
        }
        String message = "Variable '%s' not found in: s%";
        throw new IllegalArgumentException(format(message, name, variables));
    }

    public boolean containsVariable(String name) {
        if (name == null) {
            throw nullArgumentError("name");
        }
        return variables.containsKey(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tdlInternal == null) ? 0 : tdlInternal.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tdl other = (Tdl) obj;
        if (tdlInternal == null) {
            if (other.tdlInternal != null) {
                return false;
            }
        } else if (!tdlInternal.equals(other.tdlInternal)) {
            return false;
        }
        return true;
    }

}
