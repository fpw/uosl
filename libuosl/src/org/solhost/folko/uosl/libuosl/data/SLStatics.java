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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.libuosl.util.ObjectLister;

public class SLStatics implements ObjectLister {
    private final SLDataFile staticsFile, staticsIndex;

    public SLStatics(String staticsPath, String staIdxPath) throws IOException {
        staticsFile = new SLDataFile(staticsPath, true);
        staticsIndex = new SLDataFile(staIdxPath, true);
    }

    private synchronized List<SLStatic> getStatics(int cell) {
        List<SLStatic> res = new LinkedList<SLStatic>();
        int idxOffset = cell * 12;
        staticsIndex.seek(idxOffset);
        long staticsOffset = staticsIndex.readUDWord();
        long staticsCount = staticsIndex.readUDWord() / 11;

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

    public synchronized List<SLStatic> getStatics(Point2D pos) {
        List<SLStatic> res = new ArrayList<SLStatic>(10);
        for(SLStatic stat : getStatics(pos.getCellIndex())) {
            if(stat.getLocation().getX() == pos.getX() && stat.getLocation().getY() == pos.getY()) {
                res.add(stat);
            }
        }
        return res;
    }

    public Map<Long, SLStatic> getAllStatics() {
        Map<Long, SLStatic> res = new HashMap<Long, SLStatic>();
        for(int cell = 0; cell < 16384; cell++) {
            for(SLStatic stat : getStatics(cell)) {
                res.put(stat.getSerial(), stat);
            }
        }
        return res;
    }

    @Override
    public List<SLStatic> getStaticsAndDynamicsAtLocation(Point2D loc) {
        return getStatics(loc);
    }
}
