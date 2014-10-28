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

public class AllowMovePacket extends SLPacket {
    public static final short ID = 0x40;
    private short sequence;

    private AllowMovePacket() {
    }

    public AllowMovePacket(short sequence) {
        initWrite(ID, 5);
        addUByte(sequence);
    }

    public static AllowMovePacket read(ByteBuffer buf, int dataLength) {
        AllowMovePacket res = new AllowMovePacket();
        res.sequence = readUByte(buf);
        return res;
    }

    public short getSequence() {
        return sequence;
    }

    @Override
    public short getID() {
        return ID;
    }
}
