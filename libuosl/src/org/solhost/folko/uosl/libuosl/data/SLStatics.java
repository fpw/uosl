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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class SLStatics {
    private final SLDataFile staticsFile, staticsIndex;
    private List<SLStatic>[] staticCells;
    private boolean cached;

    public SLStatics(String staticsPath, String staIdxPath) throws IOException {
        staticsFile = new SLDataFile(staticsPath, true);
        staticsIndex = new SLDataFile(staIdxPath, true);
        cached = false;
    }

    @SuppressWarnings("unchecked")
    public void buildCache() {
        staticCells = new List[SLMap.MAP_HEIGHT / 8 * SLMap.MAP_HEIGHT / 8];
        for(int i = 0; i < SLMap.CELL_COUNT; i++) {
            staticCells[i] = getStatics(i);
        }
        cached = true;
    }

    private List<SLStatic> getStatics(int cell) {
        if(cached) {
            return staticCells[cell];
        }

        synchronized(this) {
            int idxOffset = cell * 12;
            staticsIndex.seek(idxOffset);
            long staticsOffset = staticsIndex.readUDWord();
            int staticsCount = (int) staticsIndex.readUDWord() / 11;
            List<SLStatic> res = new ArrayList<SLStatic>(staticsCount);

            if(staticsOffset == -1) { // no statics
                return res;
            }

            staticsFile.seek((int) staticsOffset);

            for(int i = 0; i < staticsCount; i++) {
                long serial = staticsFile.readUDWord();
                int staticID = staticsFile.readUWord();
                byte xOffset = staticsFile.readSByte();
                byte yOffset = staticsFile.readSByte();
                byte z = staticsFile.readSByte();
                int hue = staticsFile.readUWord();

                Point3D location = new Point3D(Point2D.fromCell(cell, xOffset, yOffset), z);
                SLStatic sta = new SLStatic(serial, staticID, location, hue);
                res.add(sta);
            }
            return res;
        }
    }

    public Stream<SLStatic> getStaticsStream(Point2D pos) {
        return getStatics(pos.getCellIndex())
                    .stream()
                    .filter((sta) -> sta.getLocation().equals2D(pos));
    }

    public Stream<SLStatic> getStaticsStream(int cell) {
        return getStatics(cell).stream();
    }

    public List<SLStatic> getStatics(Point2D pos) {
        return getStaticsStream(pos).collect(Collectors.toCollection(ArrayList::new));
    }

    public Map<Long, SLStatic> getAllStatics() {
        Map<Long, SLStatic> res = new HashMap<Long, SLStatic>();
        for(int cell = 0; cell < SLMap.CELL_COUNT; cell++) {
            for(SLStatic stat : getStatics(cell)) {
                res.put(stat.getSerial(), stat);
            }
        }
        return res;
    }
}
