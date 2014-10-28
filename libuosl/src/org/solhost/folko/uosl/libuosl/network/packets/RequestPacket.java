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

public class RequestPacket extends SLPacket {
    public static final short ID = 0x64;
    public static final short MODE_COUNT1 = 0x00;
    public static final short MODE_COUNT0 = 0x03;
    public static final short MODE_STATS  = 0x04;
    public static final short MODE_SKILLS = 0xFE;
    private short mode;
    private long serial;

    public static RequestPacket read(ByteBuffer buffer, int length) {
        RequestPacket res = new RequestPacket();

        res.mode = readUByte(buffer);
        res.serial = readUDWord(buffer);

        return res;
    }

    @Override
    public short getID() {
        return ID;
    }

    public short getMode() {
        return mode;
    }

    public long getSerial() {
        return serial;
    }
}
