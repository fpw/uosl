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

public class InitPlayerPacket extends SLPacket {
    public static final short ID = 0x36;
    private long serial, seed;

    public InitPlayerPacket(SendableMobile player, long seed) {
        initWrite(ID, 0x1A);

        addUDWord(player.getSerial());
        addUDWord(seed);
        // unknown stuff
        addUWord(0);
        addUWord(0);
        addUWord(0);
        addUWord(0);
        addUByte((short) 0);
        addUByte((short) 0);
        addUDWord(0);
    }

    private InitPlayerPacket() {
    }

    public static InitPlayerPacket read(ByteBuffer buffer, int length) {
        InitPlayerPacket res = new InitPlayerPacket();
        res.serial = readUDWord(buffer);
        res.seed = readUDWord(buffer);
        readUWord(buffer);
        readUWord(buffer);
        readUWord(buffer);
        readUWord(buffer);
        readUByte(buffer);
        readUByte(buffer);
        readUDWord(buffer);
        return res;
    }

    public long getSerial() {
        return serial;
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public short getID() {
        return ID;
    }
}
