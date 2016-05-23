// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import java.util.List;

class TdlValidationResult {
    public final boolean isValid;
    public final String errorSummary;
    public final List<TdlFileFieldErrorDescriptor> fieldErrorDescriptors;

    TdlValidationResult() {
        this(true, null, null);
    }

    TdlValidationResult(boolean isValid, String errorSummary,
            List<TdlFileFieldErrorDescriptor> fieldErrorDescriptors) {
        this.isValid = isValid;
        this.errorSummary = errorSummary;
        this.fieldErrorDescriptors = fieldErrorDescriptors;
    }

    @Override
    public String toString() {
        return "TdlValidationToolResult{" +
                "isValid=" + isValid +
                ", errorSummary=" + errorSummary +
                ", fieldErrorDescriptors=" + fieldErrorDescriptors +
                '}';
    }
}
