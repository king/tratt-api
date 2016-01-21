package com.king.tratt;

import com.king.tratt.spi.EventMetaData;
import com.king.tratt.spi.FieldMetaData;

public interface EventMd extends EventMetaData {

    FieldMetaData getField(String fieldName);

    //    Collection<FieldMetaData> getFields();

}
