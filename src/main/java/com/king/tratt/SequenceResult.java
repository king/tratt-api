package com.king.tratt;

import java.util.ArrayList;
import java.util.List;



public final class SequenceResult {

    private final boolean isValid;
    private final String name;
    private final List<Cause> causes = new ArrayList<>();

    public enum Cause {
        NOT_STARTED, NOT_CLOSED, TIMEOUT, INVALID_FIELDS, VALID
    };

    SequenceResult(String name, boolean isValid, List<Cause> causes) {
        this.name = name;
        this.isValid = isValid;
        this.causes.addAll(causes);
    }

    public boolean isValid() {
        return isValid;
    }

    public String getName() {
        return name;
    }

    public List<Cause> getCauses() {
        return new ArrayList<>(causes);
    }

    @Override
    public String toString() {
        String template = "Sequence '%s' is %s, with causes: %s";
        return String.format(template, name, (isValid ? "VALID" : "INVALID"), causes);
    }

}
