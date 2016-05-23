// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

public class TdlFileFieldErrorDescriptor {
    public final String errorDescription;
    public final String errorNodePath;

    TdlFileFieldErrorDescriptor(String errorDescription, String errorNodePath) {
        this.errorDescription = errorDescription;
        this.errorNodePath = errorNodePath;
    }
}
