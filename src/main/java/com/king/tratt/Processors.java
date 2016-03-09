package com.king.tratt;

import java.util.concurrent.CopyOnWriteArrayList;

class Processors {

    private CopyOnWriteArrayList<? extends SequenceProcessor<?>> processors;

    Processors(CopyOnWriteArrayList<? extends SequenceProcessor<?>> processors) {
        this.processors = processors;
    }

    boolean removeProcessor(String name) {
        return processors.removeIf(p -> p.getName().equals(name));
    }

}
