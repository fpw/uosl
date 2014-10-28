package org.solhost.folko.uosl.libuosl.network;

import java.util.HashMap;
import java.util.Map;

import org.solhost.folko.uosl.libuosl.types.Attribute;
import org.solhost.folko.uosl.libuosl.types.Direction;

public class MobileStub extends ObjectStub implements SendableMobile {
    private final Map<Attribute, Long> attributes;
    private Direction facing;

    public MobileStub() {
        attributes = new HashMap<>();
    }

    public void setAttribute(Attribute attr, long value) {
        attributes.put(attr, value);
    }

    @Override
    public long getAttribute(Attribute attr) {
        return attributes.get(attr);
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    @Override
    public Direction getFacing() {
        return facing;
    }
}
