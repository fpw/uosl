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

public class SoundPacket extends SLPacket {
    public static final short ID = 0xB8;
    private int soundID;


    private SoundPacket() {
    }

    public SoundPacket(int soundID) {
        initWrite(ID, 0x0F);
        addUByte((short) 0);
        addUWord(soundID);
        addUWord(0);
        addUWord(0);
        addUWord(0);
        addUWord(0);
    }

    public static SoundPacket read(ByteBuffer b, int len) {
        SoundPacket res = new SoundPacket();
        readUByte(b);
        res.soundID = readUWord(b);
        readUWord(b);
        readUWord(b);
        readUWord(b);
        readUWord(b);
        return res;
    }

    public int getSoundID() {
        return soundID;
    }

    @Override
    public short getID() {
        return ID;
    }
}
