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

public class SLPalette {
    private final SLDataFile palette;

    public SLPalette(String path) throws IOException {
        palette = new SLDataFile(path, true);
    }

    public synchronized  int getRGB(short index) {
        int offset = index * 3;
        palette.seek(offset);
        short r = palette.readUByte();
        short g = palette.readUByte();
        short b = palette.readUByte();
        return (r << 16) | (g << 8) | b;
    }
}
