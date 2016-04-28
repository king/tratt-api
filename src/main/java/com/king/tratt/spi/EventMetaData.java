/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

/**
 * This class represents a specific event and also maps an event name to an id.
 * <p>
 * if the event id is always the same as the event name, there is an convenient
 * implementation:
 * {@link EventMetaDataFactory#equalNameAndIdEventMetaData(String)}
 *
 * @see EventMetaDataFactory#equalNameAndIdEventMetaData(String)
 *
 */
public interface EventMetaData {
    /**
     * @return the id of this event
     */
    String getId();

    /**
     * @return the name of this event.
     */
    String getName();
}
