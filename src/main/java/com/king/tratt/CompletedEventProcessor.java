/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CompletedEventProcessor {

    private final Map<String, SequenceResult> sequenceResults = new HashMap<>();
    private final boolean isValid;

    CompletedEventProcessor() {
        isValid = false;
    }

    CompletedEventProcessor(List<SequenceResult> seqResults) {
        for (SequenceResult seqResult : seqResults) {
            sequenceResults.put(seqResult.getName(), seqResult);
        }
        isValid = isValid(seqResults);
    }

    private boolean isValid(List<SequenceResult> seqResults) {
        for (SequenceResult seqResult : seqResults) {
            if (!seqResult.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return false, if at least one {@link Sequence} has failed.
     */
    public boolean isValid() {
        return isValid;
    }

    public boolean containsSequenceResult(String name) {
        if (name == null) {
            throw new NullPointerException("Argument is null.");
        }
        return sequenceResults.containsKey(name);
    }

    public SequenceResult getSequenceResultByName(String name) {
        if (name == null) {
            throw new NullPointerException("Argument is null.");
        }
        if (!containsSequenceResult(name)) {
            throw new IllegalArgumentException("No such Sequence: " + name);
        }
        return sequenceResults.get(name);
    }

    public List<SequenceResult> getSequenceResults() {
        return new ArrayList<>(sequenceResults.values());
    }

}
