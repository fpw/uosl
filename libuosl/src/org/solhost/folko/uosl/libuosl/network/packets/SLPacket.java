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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class SLPacket {
    private static final short SL_PACKET_MAGIC = 0xFE;
    private ByteBuffer sendBuffer;

    protected void initWrite(short id, int len) {
        sendBuffer = ByteBuffer.allocate(len);
        sendBuffer.order(ByteOrder.BIG_ENDIAN);
        addUByte(SL_PACKET_MAGIC);
        addUByte(id);
        addUWord(len);
    }

    public abstract short getID();

    public void addSByte(byte b) {
        sendBuffer.put(b);
    }

    public void addUByte(short b) {
        sendBuffer.put((byte) b);
    }

    public void addSWord(short w) {
        sendBuffer.putShort(w);
    }

    public void addUWord(int w) {
        sendBuffer.putShort((short) w);
    }

    public void addSDWord(int d) {
        sendBuffer.putInt(d);
    }

    public void addUDWord(long d) {
        sendBuffer.putInt((int) d);
    }

    public void addString(String s, int len) {
        for(int i = 0; i < len; i++) {
            if(s != null && i < s.length()) {
                char c = s.charAt(i);
                addUByte((short) (c & 0xFF));
            } else {
                addUByte((short) 0);
            }
        }
    }

    public void addString(String s) {
        addString(s, s.length() + 1);
    }

    public static byte readSByte(ByteBuffer b) {
        return b.get();
    }

    public static short readUByte(ByteBuffer b) {
        return (short) (b.get() & 0xFF);
    }

    public static short readSWord(ByteBuffer b) {
        return b.getShort();
    }

    public static int readUWord(ByteBuffer b) {
        return b.getShort() & 0xFFFF;
    }

    public static int readSDWord(ByteBuffer b) {
        return b.getInt();
    }

    public static long readUDWord(ByteBuffer b) {
        return b.getInt() & 0xFFFFFFFF;
    }

    public static String readString(ByteBuffer b, int len) {
        StringBuilder res = new StringBuilder(len);
        boolean gotNull = false;
        for(int i = 0; i < len; i++) {
            char chr = (char) b.get();
            if(chr != '\0' && !gotNull) {
                res.append(chr);
            } else {
                gotNull = true;
            }
        }
        return res.toString();
    }

    public static String readString(ByteBuffer b) {
        StringBuilder res = new StringBuilder();
        char chr;
        do {
            chr = (char) b.get();
            if(chr != '\0') {
                res.append(chr);
            }
        } while(chr != '\0');
        return res.toString();
    }

    // write packet to buffer
    public void writeTo(ByteBuffer dest) throws IOException {
        sendBuffer.flip();
        dest.put(sendBuffer);
        if(sendBuffer.remaining() > 0) {
            throw new IOException("send buffer overflow");
        }
    }

    public static SLPacket readPacket(ByteBuffer buffer) throws IOException {
        int gotBytes = buffer.remaining();
        if(gotBytes < 4) {
            // not enough bytes to read packet header
            return null;
        }

        short magic = readUByte(buffer);
        if(magic != SL_PACKET_MAGIC) {
            // not an UOSL packet or packet error -> need to kick client
            throw new IOException("invalid packet magic: " + magic);
        }
        short id = readUByte(buffer);
        int length = readUWord(buffer);
        if(length > gotBytes) {
            // not enough bytes to read entire packet
            return null;
        }
        int dataLength = length - 4;

        switch(id) {
        case LoginPacket.ID:            return LoginPacket.read(buffer, dataLength);
        case LoginErrorPacket.ID:       return LoginErrorPacket.read(buffer, dataLength);
        case InitPlayerPacket.ID:       return InitPlayerPacket.read(buffer, dataLength);
        case StatsUpdatePacket.ID:      return StatsUpdatePacket.read(buffer, dataLength);
        case LocationPacket.ID:         return LocationPacket.read(buffer, dataLength);
        case EquipPacket.ID:            return EquipPacket.read(buffer, dataLength);
        case GlobalLightLevelPacket.ID: return GlobalLightLevelPacket.read(buffer, dataLength);
        case SendTextPacket.ID:         return SendTextPacket.read(buffer, dataLength);
        case SendObjectPacket.ID:       return SendObjectPacket.read(buffer, dataLength);
        case RemoveObjectPacket.ID:     return RemoveObjectPacket.read(buffer, dataLength);
        case AllowMovePacket.ID:        return AllowMovePacket.read(buffer, dataLength);
        case DenyMovePacket.ID:         return DenyMovePacket.read(buffer, dataLength);
        case SoundPacket.ID:            return SoundPacket.read(buffer, dataLength);
        case OpenGumpPacket.ID:         return OpenGumpPacket.read(buffer, dataLength);
        case FullItemsContainerPacket.ID: return FullItemsContainerPacket.read(buffer, dataLength);
        case ItemInContainerPacket.ID:  return ItemInContainerPacket.read(buffer, dataLength);
        case DragPacket.ID:             return DragPacket.read(buffer, dataLength);
        case DropPacket.ID:             return DropPacket.read(buffer, dataLength);
        case SingleClickPacket.ID:      return SingleClickPacket.read(buffer, dataLength);
        case ActionPacket.ID:           return ActionPacket.read(buffer, dataLength);
        case EquipReqPacket.ID:         return EquipReqPacket.read(buffer, dataLength);
        case MoveRequestPacket.ID:      return MoveRequestPacket.read(buffer, dataLength);
        case DoubleClickPacket.ID:      return DoubleClickPacket.read(buffer, dataLength);
        case SpeechRequestPacket.ID:    return SpeechRequestPacket.read(buffer, dataLength);
        case RequestPacket.ID:          return RequestPacket.read(buffer, dataLength);
        case ShopPacket.ID:             return ShopPacket.read(buffer, dataLength);
        case AttackPacket.ID:           return AttackPacket.read(buffer, dataLength);
        case BoardAddPostPacket.ID:     return BoardAddPostPacket.read(buffer, dataLength);
        case GroupPacket.ID:            return GroupPacket.read(buffer, dataLength);
        default:                        return UnknownPacket.read(buffer, id, dataLength);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(sendBuffer != null) {
            builder.append(String.format("Packet %02X -> ", getID()));
            for(int i = 0; i < sendBuffer.capacity(); i++) {
                builder.append(String.format("%02X", sendBuffer.get(i)));
            }
        } else {
            builder.append(getClass().getSimpleName());
        }
        return builder.toString();
    }

    public static SLPacket fromHexString(short id, String data) {
        return new ASCIIPacket(id, data);
    }
 }

class ASCIIPacket extends SLPacket {
    private final short ID;

    public ASCIIPacket(short id, String data) {
        this.ID = id;
        if(data.length() % 2 != 0) {
            throw new IllegalArgumentException("string must be of even length");
        }
        initWrite(ID, 4 + data.length() / 2);
        for(int i = 0; i < data.length(); i += 2) {
            String byS = data.substring(i, i + 2);
            short by = Short.valueOf(byS, 16);
            addUByte(by);
        }
    }

    @Override
    public short getID() {
        return ID;
    }
}
