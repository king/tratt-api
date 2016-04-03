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
