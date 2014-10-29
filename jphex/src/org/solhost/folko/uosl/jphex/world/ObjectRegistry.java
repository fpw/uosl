/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.uosl.jphex.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.solhost.folko.uosl.jphex.types.Item;
import org.solhost.folko.uosl.jphex.types.Mobile;
import org.solhost.folko.uosl.jphex.types.Player;
import org.solhost.folko.uosl.jphex.types.SLObject;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.types.Items;
import org.solhost.folko.uosl.libuosl.types.Mobiles;

// singleton that provides serial lookup and registration
// it contains all objects that appear in the game
public class ObjectRegistry {
    private static final Logger log = Logger.getLogger("jphex.registry");
    private static ObjectRegistry instance;
    private final CopyOnWriteArraySet<SerialObserver> observers;
    private final Map<Long, SLObject> objects;
    private final Map<Long, SLStatic> statics;
    private long nextItemSerial, nextMobileSerial;

    public interface SerialObserver {
        public void onObjectRegistered(SLObject object);
    }

    private ObjectRegistry(Map<Long, SLStatic> statics, Map<Long, SLObject> objects) {
        this.observers = new CopyOnWriteArraySet<SerialObserver>();
        this.statics = statics;
        this.objects = objects;
        this.nextItemSerial = Items.SERIAL_FIRST;
        this.nextMobileSerial = Mobiles.SERIAL_FIRST;

        // count stuff so we know the next free serials
        for(SLStatic stat : statics.values()) {
            long serial = stat.getSerial();
            if(serial >= nextItemSerial) {
                nextItemSerial = serial + 1;
            }
        }

        for(SLObject obj : objects.values()) {
            long serial = obj.getSerial();
            if(obj instanceof Mobile) {
                if(serial >= nextMobileSerial) {
                    nextMobileSerial = serial + 1;
                }
            } else if(obj instanceof Item) {
                if(serial >= nextItemSerial) {
                    nextItemSerial = serial + 1;
                }
            }
        }
        log.config(String.format("Initialized with %d dynamic and %d static objects", objects.size(), statics.size()));
        log.fine(String.format("Next item serial 0x%08X, next mobile serial 0x%08X", nextItemSerial, nextMobileSerial));
    }

    // set next usable item and mobile serials
    public static void init(Map<Long, SLStatic> statics, Map<Long, SLObject> objects) {
        if(instance != null) {
            throw new UnsupportedOperationException("already initialized");
        }
        instance = new ObjectRegistry(statics, objects);
    }

    public static ObjectRegistry get() {
        if(instance == null) {
            throw new UnsupportedOperationException("not initialized");
        }
        return instance;
    }

    public synchronized void addObserver(SerialObserver observer) {
        observers.add(observer);
    }

    public synchronized long registerItemSerial() {
        return nextItemSerial++;
    }

    public synchronized long registerMobileSerial() {
        return nextMobileSerial++;
    }

    public synchronized void registerObject(SLObject object) {
        long serial = object.getSerial();
        if(object.isDeleted()) {
            log.warning(String.format("attempt to register deleted object prevented: %08X", serial));
            return;
        }

        if(objects.containsKey(serial)) {
            log.warning(String.format("attempt to register object twice prevented: %08X", serial));
        }
        objects.put(serial, object);
        for(SerialObserver o : observers) {
            o.onObjectRegistered(object);
        }
    }

    public synchronized Item findItem(long serial) {
        SLObject obj = objects.get(serial);
        if(obj instanceof Item) {
            return (Item) obj;
        } else {
            return null;
        }
    }

    public synchronized Mobile findMobile(long serial) {
        SLObject obj = objects.get(serial);
        if(obj instanceof Mobile) {
            return (Mobile) obj;
        } else {
            return null;
        }
    }

    public synchronized Player findPlayer(String name) {
        for(SLObject obj : objects.values()) {
            String objName = obj.getName();
            if(!(obj instanceof Player) || objName == null)  {
                continue;
            }
            if(objName.toLowerCase().equals(name.toLowerCase())) {
                return (Player) obj;
            }
        }
        return null;
    }

    public synchronized Player findPlayer(long serial) {
        SLObject obj = objects.get(serial);
        if(obj instanceof Player) {
            return (Player) obj;
        } else {
            return null;
        }
    }

    public synchronized SLObject findObject(long serial) {
        return objects.get(serial);
    }

    // only World should use this, others should do object.delete(), hence package-private
    synchronized void removeObject(long serial) {
        objects.remove(serial);
    }

    // only World should use this, others should have a serial reference or something
    synchronized Collection<SLObject> allObjects() {
        return new ArrayList<SLObject>(objects.values());
    }

    public synchronized SLStatic findStatic(long serial) {
        return statics.get(serial);
    }

}
