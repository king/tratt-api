package com.king.tratt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InvalidTdlException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final Gson PRETTY_WRITER = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final TdlValidationResult result;

    InvalidTdlException(TdlValidationResult result) {
        super(toPrettyString(result));
        this.result = result;

    }

    private static String toPrettyString(TdlValidationResult result) {
        return PRETTY_WRITER.toJson(result);
    }

    public TdlValidationResult getResult() {
        return result;
    }


}
