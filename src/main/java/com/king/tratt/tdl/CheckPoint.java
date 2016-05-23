// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt.tdl;

import java.util.List;

import com.king.tratt.tdl.TdlInternal.CheckPointInternal;

public final class CheckPoint {

    CheckPointInternal cpInternal;

    CheckPoint(CheckPointInternal cp) {
        cpInternal = cp;
    }

    public String getEventType() {
        return cpInternal.eventType;
    }

    public String getMatch() {
        return cpInternal.match;
    }

    public String getValidate() {
        return cpInternal.validate;
    }

    public List<String> getSet() {
        return cpInternal.set;
    }

    @Override
    public String toString() {
        return cpInternal.toString();
    }

}
