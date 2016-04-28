/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt.spi;

/**
 * Example (pseudo code)
 *
 * <pre>
 * public EventMetaData getEventMetaData(String eventName) {
 *     if (isKnownEvent(eventName)) {
 *         return getKnownEventMetaData(eventName);
 *     } else {
 *         return unknownEventType();
 *     }
 * }
 * </pre>
 *
 */
@FunctionalInterface
public interface EventMetaDataFactory {

    /**
     * This method receives an argument {@code eventName} that may or may not
     * correspond to an event in your event structure.
     * <p>
     * If {@code eventName} is recognized as a known event type, a corresponding
     * {@link EventMetaData} instance should be returned.
     * <p>
     * If {@code EventName} is not recognized as a known event type,
     * {@code null} should be returned. Note! Instead of returning {@code null}
     * you can return the value from method {@link #unknownEventType()} to make
     * your code more readable.
     * <p>
     * Note:</br>
     * If your event structure does not specify a unique id for each event type
     * there is a convenient method that returns an {@link EventMetaData}
     * instance that maps id to name. Use it only for your known event types.
     * See {@link #equalNameAndIdEventMetaData(String)}.
     *
     * @param eventName
     *            a string that may or may NOT correspond to a known event type
     *            in your event structure.
     * @return {@link EventMetaData} or {@code null}.
     */
    EventMetaData getEventMetaData(String eventName);

    /**
     * Returns the special meaning value {@code null}, which means this factory
     * does not recognize the {@code eventName} in
     * {@link #getEventMetaData(String)} as a known event. Use this method
     * instead of returning {@code null}, to make your code more readable.
     *
     * @return null
     */
    default EventMetaData unknownEventType() {
        return null;
    }

    /**
     * Returns a convenient {@link EventMetaData} implementation that maps
     * {@code id} to {@code eventName}.
     * <p>
     * NOTE!</br>
     * Use this only for KNOWN event types.
     *
     * @param eventName
     *            name of a known event type.
     * @return {@link EventMetaData}.
     */
    default EventMetaData equalNameAndIdEventMetaData(String eventName) {
        return new EventMetaData() {

            @Override
            public String getName() {
                return eventName;
            }

            @Override
            public String getId() {
                return eventName;
            }
        };
    }
}
