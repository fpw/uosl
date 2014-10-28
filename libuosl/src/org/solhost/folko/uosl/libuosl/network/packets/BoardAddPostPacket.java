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

public class BoardAddPostPacket extends SLPacket {
    public static final short ID = 0xB6;
    private String subject, message;

    public static BoardAddPostPacket read(ByteBuffer b, int len) {
        BoardAddPostPacket res = new BoardAddPostPacket();
        res.subject = readString(b, 80);
        res.message = readString(b);
        return res;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public short getID() {
        return ID;
    }
}
