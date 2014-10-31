package org.solhost.folko.uosl.slclient.views;

import java.util.HashMap;
import java.util.Map;

import org.solhost.folko.uosl.slclient.models.SLObject;

// TODO: not the best data structure for this, potential for optimization

public class PickList {
    private final Map<Integer, SLObject> pickEntries;
    private int nextId;
    private boolean isValid;

    public PickList() {
        pickEntries = new HashMap<>(1000);
        isValid = false;
    }

    public void setValid(boolean valid) {
        this.isValid = valid;
        if(!valid) {
            clear();
        }
    }

    public boolean isValid() {
        return isValid;
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
        if(!isValid) {
            throw new RuntimeException("pick from invalid list");
        }
        return pickEntries.get(pickId);
    }
}
