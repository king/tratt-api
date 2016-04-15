/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
package com.king.tratt;

import java.util.concurrent.CopyOnWriteArrayList;

public final class Processors {

    private final CopyOnWriteArrayList<SequenceProcessor> processors;

    Processors(CopyOnWriteArrayList<SequenceProcessor> processors) {
        this.processors = processors;
    }

    public boolean removeProcessor(String name) {
        return processors.removeIf(p -> p.getName().equals(name));
    }

}
