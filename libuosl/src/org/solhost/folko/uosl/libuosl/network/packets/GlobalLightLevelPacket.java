package org.solhost.folko.uosl.libuosl.network.packets;

import java.nio.ByteBuffer;

public class GlobalLightLevelPacket extends SLPacket {
    public static final short ID = 0xA9;
    private byte level;

    public GlobalLightLevelPacket(byte level) {
        initWrite(ID, 5);
        addSByte(level);
    }

    private GlobalLightLevelPacket() {
    }

    public static GlobalLightLevelPacket read(ByteBuffer buffer, int length) {
        GlobalLightLevelPacket res = new GlobalLightLevelPacket();
        res.level = readSByte(buffer);
        return res;
    }

    public byte getLevel() {
        return level;
    }

    @Override
    public short getID() {
        return ID;
    }
}
