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
import java.util.LinkedList;
import java.util.List;

import org.solhost.folko.uosl.libuosl.data.SLTiles.LandTile;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.libuosl.util.ObjectLister;

public class SLData {
    public static boolean DEBUG_MOVE = false;
    public static final int CHARACHTER_HEIGHT = 10; // height of a character
    private static SLData instance;
    private final String dataPath;
    private SLMap map;
    private SLPalette palette;
    private SLStatics statics;
    private SLSound sound;
    private SLArt art;
    private SLGumps gumps;
    private SLTiles tiles;

    private SLData(String dataPath) {
        this.dataPath = dataPath;
    }

    public synchronized static SLData init(String dataPath) throws IOException {
        if(instance != null) {
            throw new RuntimeException("client data already initialized");
        }
        SLData newInstance = new SLData(dataPath);
        newInstance.load();
        instance = newInstance;
        return newInstance;
    }

    public static SLData get() {
        return instance;
    }

    public void buildCaches() {
        tiles.buildCache();
        map.buildCache();
        statics.buildCache();
    }

    private void load() throws IOException {
        map = new SLMap(dataPath +          "/MAP0.MUL");
        palette = new SLPalette(dataPath +  "/PALETTE.MUL");
        statics = new SLStatics(dataPath +  "/STATICS0.MUL", dataPath + "/STAIDX0.MUL");
        sound = new SLSound(dataPath +      "/SOUND.MUL", dataPath + "/SOUNDIDX.MUL");
        art = new SLArt(dataPath +          "/ART.MUL", dataPath + "/ARTIDX.MUL", dataPath + "/ANIMDATA.MUL");
        tiles = new SLTiles(dataPath +      "/TILEDATA.MUL");
        gumps = new SLGumps(dataPath +      "/GUMPS.MUL");
    }

    // reverse engineered from the client, sub_4061A0
    // this _should_ be written more readable, but it is very important
    // that the semantic doesn't change. Otherwise, client and server would
    // become out of sync and NPCs could walk through statics or bad things like that
    private Point3D getElevatedPointReal(Point3D source, Direction dir, ObjectLister lister) {
        Point2D dest = source.getTranslated(dir);

        int currentZ = source.getZ();
        int currZp9 = currentZ + 9;
        int finalZ = -128;
        int edi = -128;
        boolean staticsAllowWalking = false;
        for(SLStatic stat : (Iterable<SLStatic>) lister.getStaticsAndDynamicsAtLocation(dest)::iterator) {
            StaticTile tile = tiles.getStaticTile(stat.getStaticID());
            int eax = stat.getLocation().getZ();
            if(eax > currZp9) {
                // starts above us -> ignore
                continue;
            }
            // is lastStat.blocks: break
            eax = 1;
            int ecx = tile.height;
            if(ecx != 0) {
                eax = ecx;
            }
            int ebx = stat.getLocation().getZ();
            int edx = eax + ebx;
            if(edx > edi) {
                if(tile.isStair()) {
                    // 40627B
                    eax = ebx + ecx;
                    if(currZp9 >= eax) {
                        ebx += ecx;
                        edi = edx;
                        staticsAllowWalking = true;
                        finalZ = ebx;
                        continue;
                    }
                }
                if(tile.isSurface()) {
                    // 406284
                    eax = ebx + ecx;
                    if(currentZ >= eax) {
                        ebx += ecx;
                        edi = edx;
                        staticsAllowWalking = true;
                        finalZ = ebx;
                        continue;
                    }
                }
                if(tile.isImpassable()) {
                    staticsAllowWalking = false;
                    edi = edx;
                    continue;
                }
                eax = currentZ;
                eax++;
                if(eax >= edx) {
                    continue;
                }
                eax = currZp9;
                eax += 3;
                if(eax < edx) {
                    continue;
                }
                staticsAllowWalking = false;
                edi = edx;
                continue;
            } else {
                if(edx != edi) {
                    continue;
                } else {
                    if(tile.isImpassable()) {
                        staticsAllowWalking = false;
                    }
                }
            }
        }
        if(edi > -128) {
            // there are statics in our way
            if(staticsAllowWalking) {
                return new Point3D(dest, finalZ);
            } else {
                return null;
            }
        }

        // statics are ok, need to check land

        // maybe todo: if lastStat.blocks: return false;

        LandTile tile = tiles.getLandTile(map.getTextureID(dest));
        if(tile.isImpassable()) {
            return null;
        }

        finalZ = map.getTileElevation(dest);

        int resX = dest.getX();
        int resY = dest.getY();
        int resZ = finalZ;

        // blacklist because of map errors where players can escape the test area
        if((resX == 432 && (resY == 724 || resY == 723 || resY == 722) && resZ == -15) ||
            (resX == 334 && resY == 707)) {
            return null;
        }

        return new Point3D(resX, resY, resZ);
    }

    // when standing at "from" and moving in direction "dir", what's the effective 3D point?
    // returns null if impassable
    public Point3D getElevatedPoint(Point3D source, Direction dir, ObjectLister lister) {
        switch(dir) {
        case NORTH:
        case WEST:
        case SOUTH:
        case EAST:
            // straight movement: only check target
            return getElevatedPointReal(source, dir, lister);
        case NORTH_EAST:
            if(getElevatedPointReal(source, Direction.NORTH, lister) == null) {
                return null;
            } else if(getElevatedPointReal(source, Direction.EAST, lister) == null) {
                return null;
            } else {
                return getElevatedPointReal(source, dir, lister);
            }
        case NORTH_WEST:
            if(getElevatedPointReal(source, Direction.NORTH, lister) == null) {
                return null;
            } else if(getElevatedPointReal(source, Direction.WEST, lister) == null) {
                return null;
            } else {
                return getElevatedPointReal(source, dir, lister);
            }
        case SOUTH_EAST:
            if(getElevatedPointReal(source, Direction.SOUTH, lister) == null) {
                return null;
            } else if(getElevatedPointReal(source, Direction.EAST, lister) == null) {
                return null;
            } else {
                return getElevatedPointReal(source, dir, lister);
            }
        case SOUTH_WEST:
            if(getElevatedPointReal(source, Direction.SOUTH, lister) == null) {
                return null;
            } else if(getElevatedPointReal(source, Direction.WEST, lister) == null) {
                return null;
            } else {
                return getElevatedPointReal(source, dir, lister);
            }
        default: return null;
        }
    }

    public SLMap getMap() {
        return map;
    }

    public SLPalette getPalette() {
        return palette;
    }

    public SLSound getSound() {
        return sound;
    }

    public SLArt getArt() {
        return art;
    }

    public SLTiles getTiles() {
        return tiles;
    }

    public SLGumps getGumps() {
        return gumps;
    }

    public SLStatics getStatics() {
        return statics;
    }

    // get all points on a direct path from src to dest
    public List<Point3D> getDirectPath(Point3D src, Point3D dest) {
        List<Point3D> path = new LinkedList<Point3D>();

        double dirVec[] = {dest.getX() - src.getX(), dest.getY() - src.getY(), dest.getZ() - src.getZ()};
        double norm = Math.sqrt(dirVec[0] * dirVec[0] + dirVec[1] * dirVec[1] + dirVec[2] * dirVec[2]);
        dirVec[0] /= norm;
        dirVec[1] /= norm;
        dirVec[2] /= norm;

        double x = src.getX(),
               y = src.getY(),
               z = src.getZ();

        Point3D addedLast = src;
        while(!addedLast.equals(dest)) {
            Point3D next = new Point3D((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));

            if(!addedLast.equals(next)) {
                path.add(next);
                addedLast = next;
            }

            x += dirVec[0];
            y += dirVec[1];
            z += dirVec[2];
        }

        return path;
    }

    public boolean hasLineOfSight(Point3D src, Point3D dest, int maxDistance, ObjectLister lister) {
        int distance = src.distanceTo(dest);
        if(distance > maxDistance) {
            return false;
        } else if(distance == 0) {
            return true;
        }

        List<Point3D> path = getDirectPath(src, dest);
        for(Point3D point : path) {
            for(SLStatic obj : (Iterable<SLStatic>) lister.getStaticsAndDynamicsAtLocation(point)::iterator) {
                StaticTile stat = tiles.getStaticTile(obj.getStaticID());
                int lowerZ = obj.getLocation().getZ();
                int upperZ = lowerZ + stat.height;
                if(lowerZ <= point.getZ() && upperZ >= point.getZ()) {
                    // there's a static that potentially blocks the way.
                    if(!stat.isSurface()) {
                        return false;
                    } else {
                        // a surface only blocks if it is between on Z
                        int surfZ = obj.getLocation().getZ();
                        if((src.getZ() < surfZ && dest.getZ() >= surfZ) || (dest.getZ() < surfZ && src.getZ() >= surfZ)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public String getDataPath() {
        return dataPath;
    }
}
