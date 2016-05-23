// (C) king.com Ltd 2016
// https://github.com/king/tratt-api
// License: Apache 2.0, https://raw.github.com/king/PROJECT/LICENSE-APACHE
package com.king.tratt.spi.test.imp;

import com.king.tratt.spi.ApiConfigurationProvider;
import com.king.tratt.spi.EventMetaDataFactory;
import com.king.tratt.spi.ValueFactory;

public class TestApiConfigurationProvider implements ApiConfigurationProvider {
    TestEventMetaDataFactory mdFactory = new TestEventMetaDataFactory();
    TestValueFactory valueFactory = new TestValueFactory(mdFactory);

    @Override
    public ValueFactory valueFactory() {
        return valueFactory;
    }

    @Override
    public EventMetaDataFactory metaDataFactory() {
        return mdFactory;
    }

}
