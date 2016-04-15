/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt.spi;

public interface ApiConfigurationProvider {

    ValueFactory valueFactory();

    EventMetaDataFactory metaDataFactory();

}
