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

public class SLSound {
    private final SLDataFile sound, soundIdx;

    public SLSound(String soundPath, String soundIdxPath) throws IOException {
        this.sound = new SLDataFile(soundPath, false);
        this.soundIdx = new SLDataFile(soundIdxPath, true);
    }

    public class SoundEntry {
        public long id;
        public long unknown;
        public String fileName;
        public byte[] pcmData;
    }

    public int getNumEntries() {
        return soundIdx.getLength() / 12;
    }

    public synchronized SoundEntry getEntry(int index) {
        int idxOffset = index * 12;
        soundIdx.seek(idxOffset);

        SoundEntry entry = new SoundEntry();

        long offset = soundIdx.readUDWord();
        long length = soundIdx.readUDWord();
        if(offset == -1 || length == -1) {
            return null;
        }

        entry.id = index;
        entry.unknown = soundIdx.readUDWord();
        sound.seek((int) offset);
        entry.fileName = sound.readString(16);
        sound.skip(24);
        entry.pcmData = sound.readRaw((int) (length - 16 - 24));

        return entry;
    }

}
