package org.solhost.folko.uosl.slclient.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.solhost.folko.uosl.libuosl.types.Point2D;

// TODO: Implement a fast custom data structure for this

public class ObjectRegistry {
    private final Map<Long, SLObject> serialMap;

    public ObjectRegistry() {
        serialMap = new HashMap<>(100);
    }

    public void registerObject(SLObject obj) {
        serialMap.put(obj.getSerial(), obj);
    }

    public void removeObject(long serial) {
        serialMap.remove(serial);
    }

    public void removeFarther(Point2D center, int radius) {
        for(Iterator<SLObject> it = serialMap.values().iterator(); it.hasNext(); ) {
            SLObject obj = it.next();
            if(obj instanceof SLItem && !((SLItem) obj).isOnGround()) {
                continue;
            }
            if(obj.getLocation().distanceTo(center) > radius) {
                it.remove();
            }
        }
    }

    public Stream<SLObject> getObjectsAt(Point2D pos) {
        return serialMap.values()
                .stream()
                .filter((obj) -> {
                    if(obj instanceof SLItem && !((SLItem) obj).isOnGround()) {
                        return false;
                    }
                    return obj.getLocation().equals2D(pos);
                });
    }

    public SLObject getObjectBySerial(long serial) {
        return serialMap.get(serial);
    }
}
