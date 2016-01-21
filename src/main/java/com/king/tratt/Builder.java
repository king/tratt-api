package com.king.tratt;

import java.util.ArrayList;
import java.util.List;

import com.king.tratt.spi.ApiConfiguratorProvider;
import com.king.tratt.spi.EventIterator;

public class Builder {

    List<EventIterator> iterators = new ArrayList<>();

    public Builder addEventIterator(EventIterator iter){
        iterators.add(iter);
        return this;
    }

    public Builder setApiConfiguratorProvider(ApiConfiguratorProvider p) {

        return this;
    }

}
