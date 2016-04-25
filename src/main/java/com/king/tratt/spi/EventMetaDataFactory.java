/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.spi;

@FunctionalInterface
public interface EventMetaDataFactory {
	
	/**
	 * TODO
	 * @param eventName
	 * @return
	 */
    EventMetaData getEventMetaData(String eventName);

    /**
     * Should return the special value null, which means this factory does not
     * recognize the given {@code eventName}
     *
     * @return null
     */
    default EventMetaData notFound() {
        return null;
    }
}
