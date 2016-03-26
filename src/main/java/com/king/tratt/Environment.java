package com.king.tratt;

import java.util.HashMap;
import java.util.Map;

import com.king.tratt.spi.Value;

/**
 * TODO get rid of this class!
 *
 * @param <E>
 */
class Environment {

    final Map<String, Value> sequenceVariables = new HashMap<>();
    final Map<String, String> tdlVariables;

    public Environment(Map<String, String> tdlVariables) {
        this.tdlVariables = tdlVariables;
    }

    @Override
    public String toString() {
        return "Environment [sequenceVariables=" + sequenceVariables + ", tdlVariables=" + tdlVariables + "]";
    }

}
