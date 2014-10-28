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
package org.solhost.folko.uosl.libuosl.network.packets;

import java.nio.ByteBuffer;

import org.solhost.folko.uosl.libuosl.types.Point3D;

public class DropPacket extends SLPacket {
    public static final short ID = 0x0F;
    public static final long CONTAINER_GROUND = 0xFFFFFFFF;
    private long serial, container;
    private Point3D location;

    public static DropPacket read(ByteBuffer b, int len) {
        DropPacket res = new DropPacket();
        res.serial = readUDWord(b);
        int x = readUWord(b);
        int y = readUWord(b);
        byte z = readSByte(b);
        if(x == 0xFFFF) {
            x = 0;
        }
        if(y == 0xFFFF) {
            y = 0;
        }
        res.location = new Point3D(x, y, z);
        res.container = readUDWord(b);
        return res;
    }

    @Override
    public short getID() {
        return ID;
    }

    public long getSerial() {
        return serial;
    }

    public long getContainer() {
        return container;
    }

    public Point3D getLocation() {
        return location;
    }
}
