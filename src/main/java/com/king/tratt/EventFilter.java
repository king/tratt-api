// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt;

import com.king.tratt.spi.Event;

@FunctionalInterface
public interface EventFilter {

    boolean accept(Event event);

}
