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

public class UnknownPacket extends SLPacket {
    private final short id;
    private final byte[] data;

    public UnknownPacket(short id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public short getID() {
        return id;
    }

    public static UnknownPacket read(ByteBuffer buffer, short id, int length) {
        byte[] bufferData = buffer.array();
        byte[] data = new byte[length];
        System.arraycopy(bufferData, buffer.arrayOffset() + buffer.position(), data, 0, length);
        buffer.position(buffer.position() + length);
        return new UnknownPacket(id, data);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Packet %02X,  length %d: ", id, data.length));
        for(int i = 0; i < data.length; i++) {
            builder.append(String.format("%02X", data[i]));
        }

        return builder.toString();
    }
}
