package org.solhost.folko.uosl.slclient.views;

import java.util.HashMap;
import java.util.Map;

import org.solhost.folko.uosl.slclient.models.SLObject;

// TODO: not the best data structure for this, potential for optimization

public class PickList {
    private final Map<Integer, SLObject> pickEntries;
    private int nextId;

    public PickList() {
        pickEntries = new HashMap<>(1000);
    }

    public void clear() {
        nextId = 1;
        pickEntries.clear();
    }

    public int enter(SLObject obj) {
        pickEntries.put(nextId, obj);
        return nextId++;
    }

    public SLObject get(int pickId) {
        return pickEntries.get(pickId);
    }
}
