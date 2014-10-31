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
    public final static int TILES_PER_CELL = 8 * 8;
    public final static int CELL_COUNT = MAP_WIDTH * MAP_HEIGHT / TILES_PER_CELL;
    private final SLDataFile mapFile;

    private boolean cached;
    private MapTile[][] tiles;

    private class MapTile {
        int textureID;
        byte elevation;
    }

    public SLMap(String map0Path) throws IOException {
        mapFile = new SLDataFile(map0Path, true);
        cached = false;
    }

    public void buildCache() {
        tiles = new MapTile[CELL_COUNT][TILES_PER_CELL];
        for(int cell = 0; cell < CELL_COUNT; cell++) {
            for(int x = 0; x < 8; x++) {
                for(int y = 0; y < 8; y++) {
                    tiles[cell][Point2D.getTileIndex(x, y)] = readTile(Point2D.fromCell(cell, x, y));
                }
            }
        }
        cached = true;
    }

    private MapTile readTile(Point2D pos) {
        if(cached) {
            return tiles[pos.getCellIndex()][pos.getTileIndex()];
        }

        synchronized(this) {
            MapTile res = new MapTile();
            int cell = pos.getCellIndex();
            int tile = pos.getTileIndex();
            int offset = cell * 196 + 4 + tile * 3;

            mapFile.seek(offset);
            res.textureID = mapFile.readUWord();
            res.elevation = mapFile.readSByte();

            return res;
        }
    }

    // experimental
    public synchronized long getColor(int cellID) {
        int offset = cellID * 196;
        mapFile.seek(offset);
        return mapFile.readUDWord();
    }

    // get the actual height as specified in the map file
    public byte getTileElevation(Point2D pos) {
        MapTile tile = readTile(pos);
        return tile.elevation;
    }

    public int getTextureID(Point2D pos) {
        MapTile tile = readTile(pos);
        return tile.textureID;
    }
}
