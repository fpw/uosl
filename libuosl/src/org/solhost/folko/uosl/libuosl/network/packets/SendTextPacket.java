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

import org.solhost.folko.uosl.libuosl.network.ObjectStub;
import org.solhost.folko.uosl.libuosl.network.SendableObject;

public class SendTextPacket extends SLPacket {
    public static final short ID = 0x37;
    public static final short MODE_SAY = 0;
    public static final short MODE_SYSMSG = 1;
    public static final short MODE_SEE = 6;
    public static final long COLOR_SYSTEM = 0x0000FF;
    public static final long COLOR_SEE_PLAYER = 0xFF7700;
    public static final long COLOR_SEE_NPC = 0xFFFFFF;

    private SendableObject source;
    private short mode;
    private long color;
    private String text;

    // src can be null when MODE_SYSMSG
    public SendTextPacket(SendableObject src, short mode, long color, String text) {
        initWrite(ID, 46 + text.length());
        if(src instanceof SendableObject) {
            addUDWord(src.getSerial());
        } else {
            addUDWord(0xFFFFFFFF);
        }
        addUWord(0); // unknown
        addUByte(mode);
        addUDWord(color);
        if(src instanceof SendableObject) {
            addString(src.getName(), 30);
        } else {
            addString("SYSTEM", 30);
        }
        addString(text);
    }

    private SendTextPacket() {
    }

    public static SendTextPacket read(ByteBuffer buffer, int length) {
        SendTextPacket res = new SendTextPacket();
        ObjectStub o = new ObjectStub();

        o.setSerial(readUDWord(buffer));
        readUWord(buffer);
        res.mode = readUByte(buffer);
        res.color = readUDWord(buffer);
        o.setName(readString(buffer, 30));
        res.text = readString(buffer);

        res.source = o;
        return res;
    }

    public SendableObject getSource() {
        return source;
    }

    public short getMode() {
        return mode;
    }

    public long getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    @Override
    public short getID() {
        return ID;
    }
}
