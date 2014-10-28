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

import org.solhost.folko.uosl.libuosl.types.Direction;

public class MoveRequestPacket extends SLPacket {
    public static final short ID = 0x04;
    private Direction direction;
    private short sequence;
    private boolean running;

    private MoveRequestPacket() {
    }

    public MoveRequestPacket(Direction dir, short sequence, boolean running) {
        initWrite(ID, 6);
        short rawDirection = dir.toByte();
        if(running) {
            rawDirection |= 0x80;
        }
        addUByte(rawDirection);
        addUByte(sequence);
    }

    public static MoveRequestPacket read(ByteBuffer buffer, int len) {
        MoveRequestPacket res = new MoveRequestPacket();
        short rawDirection = readUByte(buffer);
        res.direction = Direction.parse((short) (rawDirection & ~0x80));
        res.running = (rawDirection & 0x80) != 0;
        res.sequence = readUByte(buffer);
        return res;
    }

    public Direction getDirection() {
        return direction;
    }

    public short getSequence() {
        return sequence;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public short getID() {
        return ID;
    }
}
