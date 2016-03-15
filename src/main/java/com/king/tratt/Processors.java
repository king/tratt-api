package com.king.tratt;

import java.util.concurrent.CopyOnWriteArrayList;

// TODO make public class?
class Processors {

    private final CopyOnWriteArrayList<? extends SequenceProcessor<?>> processors;

    Processors(CopyOnWriteArrayList<? extends SequenceProcessor<?>> processors) {
        this.processors = processors;
    }

    boolean removeProcessor(String name) {
        return processors.removeIf(p -> p.getName().equals(name));
    }

}
