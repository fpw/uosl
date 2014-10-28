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

import org.solhost.folko.uosl.libuosl.types.Point2D;

public class SLMap {
    public final static int MAP_WIDTH = 1024;
    public final static int MAP_HEIGHT = 1024;
    private final SLDataFile mapFile;

    private class Tile {
        int textureID;
        byte elevation;
    }

    public SLMap(String map0Path) throws IOException {
        mapFile = new SLDataFile(map0Path, true);
    }

    private synchronized Tile readTile(Point2D pos) {
        Tile res = new Tile();
        int cell = pos.getCellIndex();
        int tile = pos.getTileIndex();
        int offset = cell * 196 + 4 + tile * 3;

        mapFile.seek(offset);
        res.textureID = mapFile.readUWord();
        res.elevation = mapFile.readSByte();

        return res;
    }

    public long getColor(int cellID) {
        int offset = cellID * 196;
        mapFile.seek(offset);
        return mapFile.readUDWord();
    }

    // get the actual height as specified in the map file
    public byte getTileElevation(int x, int y) {
        if(x < 0 || x >= 1024 || y < 0 || y >= 1024) {
            return 0;
        }
        Tile tile = readTile(new Point2D(x, y));
        return tile.elevation;
    }

    // get the average elevation based on the surrounding
    public byte getElevation(int x, int y) {
        if(x < 0 || x >= 1024 || y < 0 || y >= 1024) {
            return 0;
        }
        return getElevation(new Point2D(x, y));
    }

    // get the average elevation based on the surrounding
    public byte getElevation(Point2D pos) {
        Tile tile = readTile(pos);

        // need to check surrounding terrain because a slope has different altitudes
        // inside the tile
        int x = pos.getX();
        int y = pos.getY();
        int top = tile.elevation;
        int right = getTileElevation(x + 1,  y);
        int bottom = getTileElevation(x + 1, y + 1);
        int left = getTileElevation(x, y + 1);

        // Check if left-right or top-bottom slope is stronger
        if(Math.abs(top - bottom) > Math.abs(left - right)) {
            return (byte) ((left + right) / 2.0);
        } else {
            return (byte) ((top + bottom) / 2.0);
        }
    }

    public int getTextureID(Point2D pos) {
        Tile tile = readTile(pos);
        return tile.textureID;
    }
}
