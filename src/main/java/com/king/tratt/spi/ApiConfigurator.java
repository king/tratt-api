package com.king.tratt.spi;


public abstract class ApiConfigurator<E extends Event> {

    public abstract ValueFactory<E> getValueFactory();

    public abstract EventMetaDataProvider getEventMetaDataProvider();

}
