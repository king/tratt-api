/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

public class TdlFileFieldErrorDescriptor {
    public final String errorDescription;
    public final String errorNodePath;

    TdlFileFieldErrorDescriptor(String errorDescription, String errorNodePath) {
        this.errorDescription = errorDescription;
        this.errorNodePath = errorNodePath;
    }
}
