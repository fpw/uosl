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

import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class DenyMovePacket extends SLPacket {
    public static final short ID = 0x3F;
    private Point3D location;
    private Direction facing;
    private short deniedSequence;

    private DenyMovePacket() {
    }

    public DenyMovePacket(SendableMobile player, short sequence) {
        initWrite(ID, 0x0B);
        addUByte(sequence);
        addUWord(player.getLocation().getX());
        addUWord(player.getLocation().getY());
        addUByte(player.getFacing().toByte());
        addSByte((byte) player.getLocation().getZ());
    }

    public static DenyMovePacket read(ByteBuffer b, int dataLen) {
        DenyMovePacket res = new DenyMovePacket();
        res.deniedSequence = readUByte(b);
        int x = readUWord(b);
        int y = readUWord(b);
        res.facing = Direction.parse(readUByte(b));
        int z = readSByte(b);
        res.location = new Point3D(x, y, z);
        return res;
    }

    public short getDeniedSequence() {
        return deniedSequence;
    }

    public Direction getFacing() {
        return facing;
    }

    public Point3D getLocation() {
        return location;
    }

    @Override
    public short getID() {
        return ID;
    }
}
