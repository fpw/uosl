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

public class ActionPacket extends SLPacket {
    public static final short ID = 0x17;
    public static final short MODE_BBOARD = 0x20;
    public static final short MODE_CAST_SPELL = 0x27;
    public static final short MODE_USE_SCROLL = 0x2F;
    public static final short MODE_OPEN_SPELLBOOK = 0x43;
    private String action;
    private short mode;

    public static ActionPacket read(ByteBuffer b, int len) {
        ActionPacket res = new ActionPacket();
        res.mode = readUByte(b);
        res.action = readString(b, len - 1);
        return res;
    }

    public short getMode() {
        return mode;
    }

    public String getAction() {
        return action;
    }

    @Override
    public short getID() {
        return ID;
    }
}
