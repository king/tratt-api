// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE

package com.king.tratt.spi;

public interface ApiConfigurationProvider {

    ValueFactory valueFactory();

    EventMetaDataFactory metaDataFactory();

}
