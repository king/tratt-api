/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import com.king.tratt.spi.Event;

public interface EventFilter {

    boolean accept(Event event);

}
