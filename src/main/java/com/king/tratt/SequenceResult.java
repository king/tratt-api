// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

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

    public final boolean isValid() {
        return isValid;
    }

    public final String getName() {
        return name;
    }

    public final List<Cause> getCauses() {
        return new ArrayList<>(causes);
    }

    @Override
    public String toString() {
        String template = "Sequence '%s' is %s, with causes: %s";
        String result = isValid ? "VALID" : "INVALID";
        return String.format(template, name, result, causes);
    }

}
