package com.king.tratt.spi.test.imp;

import static java.lang.String.format;

import com.king.tratt.spi.Value;
import com.king.tratt.spi.ValueFactory;

public class TestValueFactory implements ValueFactory {

    private TestEventMetaDataFactory metaDataFactory;

    public TestValueFactory(TestEventMetaDataFactory eventMetaDataFactory) {
        this.metaDataFactory = eventMetaDataFactory;
    }

    @Override
    public Value getValue(String eventName, String parameterName) {
        TestEventMetaData metaData = (TestEventMetaData) metaDataFactory.getEventMetaData(eventName);
        if (metaData == null) {
            return notFound();
        }

        Field field = metaData.getField(parameterName);
        if (field == null) {
            return notFound();
        }

        switch (parameterName) {
        case "id":
            return new IdEventValue();
        case "timestamp":
            return new TimeStampEventValue();
        }

        final int index = field.getIndex();
        final String type = field.getType();

        if (type.matches("String")) {
            return new StringEventValue(index, field.getName());
        } else if (type.matches("[Bb]oolean")) {
            return new BooleanEventValue(index, field.getName());
        } else if (type.matches("int|Integer|[Ll]ong")) {
            return new LongEventValue(index, field.getName());
        } else {
            String message = "Unknown type '%s'. Can not decide *EventValue class from: '%s.%s'";
            throw new IllegalArgumentException(format(message, type, eventName, parameterName));
        }
    }


}
