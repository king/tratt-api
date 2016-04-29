/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
/*
 * // (C) king.com Ltd 2014
 */

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
