package com.king.tratt.spi;

@FunctionalInterface
public interface EventMetaDataFactory<T extends EventMetaData> {
	
	/**
	 * TODO
	 * @param eventName
	 * @return
	 */
    T getEventMetaData(String eventName);

    /**
     * Should return the special value null, which means this factory does not
     * recognize the given {@code eventName}
     *
     * @return null
     */
    default T notFound() {
        return null;
    }
}
