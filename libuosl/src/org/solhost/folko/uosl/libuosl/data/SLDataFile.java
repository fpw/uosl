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
package org.solhost.folko.uosl.libuosl.data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

final class SLDataFile {
    private ByteBuffer mappedFile;

    public SLDataFile(String path, boolean cacheFully) throws IOException {
        RandomAccessFile file = new RandomAccessFile(path, "r");
        if(cacheFully) {
            // read entire file to memory
            byte[] content = new byte[(int) file.length()];
            file.read(content);
            mappedFile = ByteBuffer.wrap(content);
        } else {
            // use memory mapped I/O
            FileChannel channel = file.getChannel();
            mappedFile = channel.map(MapMode.READ_ONLY, 0, file.length());
            channel.close();
        }
        mappedFile.order(ByteOrder.LITTLE_ENDIAN);
        file.close();
    }

    public void setByteOrder(ByteOrder order) {
        mappedFile.order(order);
    }

    public void seek(int offset) {
        mappedFile.position(offset);
    }

    public void skip(int bytes) {
        mappedFile.position(mappedFile.position() + bytes);
    }

    public int getLength() {
        return mappedFile.capacity();
    }

    public byte readSByte() {
        return mappedFile.get();
    }

    public short readUByte() {
        return (short) (mappedFile.get() & 0xFF);
    }

    public short readSWord() {
        return mappedFile.getShort();
    }

    public int readUWord() {
        return mappedFile.getShort() & 0xFFFF;
    }

    public int readSDWord() {
        return mappedFile.getInt();
    }

    public long readUDWord() {
        return mappedFile.getInt() & 0xFFFFFFFF;
    }

    public boolean hasMore() {
        return mappedFile.remaining() > 0;
    }

    public int getPosition() {
        return mappedFile.position();
    }

    public byte[] readRaw(int num) {
        byte[] res = new byte[num];
        mappedFile.get(res);
        return res;
    }

    public String readString() {
        StringBuilder res = new StringBuilder();
        char chr;
        do {
            chr = (char) mappedFile.get();
            if(chr != '\0') {
                res.append(chr);
            }
        } while(chr != '\0');
        return res.toString();
    }

    public String readString(int len) {
        StringBuilder res = new StringBuilder();
        char chr;
        for(int i = 0; i < len; i++) {
            chr = (char) mappedFile.get();
            if(chr != '\0') {
                res.append(chr);
            }
        }
        return res.toString();
    }
}
