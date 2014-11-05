package org.solhost.folko.uosl.slclient.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.types.Attribute;
import org.solhost.folko.uosl.libuosl.types.Direction;

public class SLMobile extends SLObject implements SendableMobile {
    private final Map<Attribute, Long> attributes;
    private final Map<Short, SLItem> equipment;
    private Direction facing;

    public SLMobile(long serial, int graphic) {
        super(serial, graphic);
        attributes = new HashMap<>();
        for(Attribute attr : Attribute.values()) {
            attributes.put(attr, 0L);
        }
        equipment = new HashMap<>();
    }

    @Override
    public long getAttribute(Attribute attr) {
        return attributes.get(attr);
    }

    public void setAttribute(Attribute attr, long value) {
        attributes.put(attr, value);
    }

    public void setFacing(Direction dir) {
        this.facing = dir;
    }

    @Override
    public Direction getFacing() {
        return facing;
    }

    public void equip(SLItem itm) {
        equipment.put(itm.getLayer(), itm);
        itm.setWorn(this);
    }

    public Collection<SLItem> getEquipment() {
        return Collections.unmodifiableCollection(equipment.values());
    }

    public void unequip(SLItem itm) {
        equipment.remove(itm.getLayer());
        itm.setWorn(null);
    }
}
