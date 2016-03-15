package com.king.tratt;

import java.util.concurrent.CopyOnWriteArrayList;

public final class Processors {

    private final CopyOnWriteArrayList<? extends SequenceProcessor<?>> processors;

    Processors(CopyOnWriteArrayList<? extends SequenceProcessor<?>> processors) {
        this.processors = processors;
    }

    public boolean removeProcessor(String name) {
        return processors.removeIf(p -> p.getName().equals(name));
    }

}
