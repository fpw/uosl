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

import org.solhost.folko.uosl.libuosl.network.MobileStub;
import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class LocationPacket extends SLPacket {
    public static final short ID = 0x3E;
    private SendableMobile mobile;

    public LocationPacket(SendableMobile player) {
        initWrite(ID, 0x13);
        addUDWord(player.getSerial());
        addUWord(player.getGraphic());
        addUByte((short) 0); // unused
        addUWord(player.getLocation().getX());
        addUWord(player.getLocation().getY());
        addUWord(0); // area
        addUByte(player.getFacing().toByte());
        addSByte((byte) player.getLocation().getZ());
    }

    private LocationPacket() {
    }

    public static LocationPacket read(ByteBuffer buffer, int length) {
        LocationPacket res = new LocationPacket();
        MobileStub m = new MobileStub();

        m.setSerial(readUDWord(buffer));
        m.setGraphic(readUWord(buffer));
        readUByte(buffer);
        int x = readUWord(buffer);
        int y = readUWord(buffer);
        readUWord(buffer);
        Direction facing = Direction.parse(readUByte(buffer));
        byte z = readSByte(buffer);
        m.setFacing(facing);
        m.setLocation(new Point3D(x, y, z));

        res.mobile = m;
        return res;
    }

    public SendableMobile getMobile() {
        return mobile;
    }

    @Override
    public short getID() {
        return ID;
    }
}
