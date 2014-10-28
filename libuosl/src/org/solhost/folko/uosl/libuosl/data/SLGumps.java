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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SLGumps {
    public static final int GUMP_ENTRY_COUNT = 65535;
    private final SortedMap<Integer, GumpEntry> gumpEntries;

    public class GumpEntry {
        public int id;
        public BufferedImage image;
    }

    public SLGumps(String gumpsPath) throws IOException {
        gumpEntries = new TreeMap<>();
        readGumps(gumpsPath);
    }

    /**
     * Sorted in ascending order
     * @return all gump IDs in ascending order
     */
    public List<Integer> getAllGumpIDs() {
        Integer[] ids = gumpEntries.keySet().toArray(new Integer[0]);
        return Collections.unmodifiableList(Arrays.asList(ids));
    }

    public GumpEntry getGump(int id) {
        return gumpEntries.get(id);
    }

    private void readGumps(String gumpsPath) throws IOException {
        // we can only read all gumps at once because there is no index file
        SLDataFile gumps = new SLDataFile(gumpsPath, false);
        for(int i = 0; i < GUMP_ENTRY_COUNT; i++) {
            boolean hasData = (gumps.readUByte() == 1);
            if(!hasData) {
                continue;
            }
            int width = gumps.readUWord();
            int height = gumps.readUWord();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    int color = gumps.readUWord();
                    image.setRGB(x, y, SLColor.convert555(color, 0xFF));
                }
            }
            GumpEntry entry = new GumpEntry();
            entry.id = i;
            entry.image = image;
            gumpEntries.put(i, entry);
        }
    }
}
