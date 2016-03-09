package com.king.tratt;

import java.util.ArrayList;
import java.util.List;

import com.king.tratt.spi._ApiConfigurator;

public class Builder {

    List<EventIterator> iterators = new ArrayList<>();

    public Builder addEventIterator(EventIterator iter){
        iterators.add(iter);
        return this;
    }

    public Builder setApiConfiguratorProvider(_ApiConfigurator p) {

        return this;
    }

}
