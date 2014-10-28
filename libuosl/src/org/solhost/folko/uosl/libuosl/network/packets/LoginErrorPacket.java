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

public class LoginErrorPacket extends SLPacket {
    public static final short ID = 0xB7;
    public static final short REASON_PASSWORD = 0;
    public static final short REASON_CHAR_NOT_FOUND = 1;
    public static final short REASON_OTHER = 2;

    private short reason;

    public LoginErrorPacket(short reason) {
        initWrite(ID, 5);
        addUByte(reason);
    }

    private LoginErrorPacket() {
    }

    public short getReason() {
        return reason;
    }

    public static SLPacket read(ByteBuffer buffer, int dataLength) {
        LoginErrorPacket res = new LoginErrorPacket();
        res.reason = readUByte(buffer);
        return res;
    }

    @Override
    public short getID() {
        return ID;
    }
}
