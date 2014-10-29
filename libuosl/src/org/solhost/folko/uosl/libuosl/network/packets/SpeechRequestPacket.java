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

public class SpeechRequestPacket extends SLPacket {
    public static final short ID = 0x06;
    public static final short MODE_BARK         = 0x00; // default, switched to with '
    public static final short MODE_GROUP        = 0x01; // initiated with /
    public static final short MODE_WHISPER      = 0x02; // initiated with :
    public static final short MODE_BROADCAST    = 0x03; // initiated with t
    public static final short MODE_CRY          = 0x04; // initiated with "

    private String text;
    private short mode;
    private long color;

    private SpeechRequestPacket() {
    }

    public SpeechRequestPacket(String text, long color, short mode) {
        initWrite(ID, 9 + text.length() + 1);
        addUByte(mode);
        addUDWord(color);
        addString(text);
    }

    public static SpeechRequestPacket read(ByteBuffer buffer, int len) {
        SpeechRequestPacket res = new SpeechRequestPacket();
        res.mode = readUByte(buffer);
        res.color = readUDWord(buffer);
        res.text = readString(buffer, len - 5);
        return res;
    }

    @Override
    public short getID() {
        return ID;
    }

    public short getMode() {
        return mode;
    }

    public String getText() {
        return text;
    }

    public long getColor() {
        return color;
    }
}
