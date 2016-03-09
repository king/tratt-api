package com.king.tratt.test.imp;

import static java.lang.String.format;

import com.king.tratt.Value;
import com.king.tratt.ValueFactory;

public class TestValueFactory implements ValueFactory<TestEvent> {

    private TestEventMetaDataFactory metaDataFactory;

    public TestValueFactory(TestEventMetaDataFactory eventMetaDataFactory) {
        this.metaDataFactory = eventMetaDataFactory;
    }

    @Override
    public Value<TestEvent> getValue(String eventName, String parameterName) {
        TestEventMetaData metaData = metaDataFactory.getEventMetaData(eventName);
        if (metaData == null) {
            return unrecognizedValue();
        }

        Field field = metaData.getField(parameterName);
        if (field == null) {
            return unrecognizedValue();
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
