/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InvalidTdlException extends TrattException {

    private static final long serialVersionUID = 1L;
    private static final Gson PRETTY_WRITER = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    InvalidTdlException(TdlValidationResult result) {
        super(toPrettyString(result));
    }

    private static String toPrettyString(TdlValidationResult result) {
        return PRETTY_WRITER.toJson(result);
    }
}
