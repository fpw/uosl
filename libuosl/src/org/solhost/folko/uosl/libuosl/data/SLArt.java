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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.uosl.libuosl.types.Direction;

public class SLArt {
    public static final int NUM_LAND_ARTS = 0x4000;
    public static final int NUM_STATIC_ARTS = 0x4000;
    public static final int NUM_ANIMATION_ARTS = 0x4000;
    public static final int TILE_DIAMETER = 44;
    private final SLDataFile artData, artIdx, animData;

    public SLArt(String artPath, String idxPath, String animDataPath) throws IOException {
        artData = new SLDataFile(artPath, false);
        artIdx = new SLDataFile(idxPath, true);
        animData = new SLDataFile(animDataPath, true);
    }

    public class ArtEntry {
        public int id;
        public long unknown;
        public BufferedImage image;
        public void mirror(boolean mirror) {
            if(mirror) {
                BufferedImage m = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                Graphics g = m.createGraphics();
                g.drawImage(image, image.getWidth(), 0, -image.getWidth(), image.getHeight(), null);
                g.dispose();
                image = m;
            }
        }
    }

    public class MobileAnimation {
        public int id;
        public boolean needMirror;
        public List<Integer> frames; // static tiles
    }

    // this is for *static* animations like fire on ground when the animation flag is set
    public class ItemAnimation {
        public byte[] frameOffsets;

        // used by the client as current frame index in the table, i.e. the info in the file is probably start index of the table
        public short currentIndex;

        // maximum index in the frame table
        public short maxIndex;

        // can be 1, 6 or 8 (and -84 for id 0x10F)
        public short unknown1;

        // can be 1..5 (and 18 for id 0x10F), defaults to 8 before the client overwrites it with the animdata information
        public short unknown2;
    }


    private synchronized Integer getArtFileOffset(int artID) {
        long idxOffset = artID * 12;
        artIdx.seek((int) idxOffset);
        long offset = artIdx.readUDWord();
        long length = artIdx.readUDWord();

        if(offset == -1 || length == -1) {
            return null;
        }
        return (int) offset;
    }

    public synchronized ArtEntry getLandArt(int landID) {
        Integer offset = getArtFileOffset(landID);
        if(offset == null) {
            return null;
        }
        artData.seek(offset);
        ArtEntry entry = new ArtEntry();
        entry.unknown = artIdx.readUDWord();
        entry.id = landID;
        entry.image = new BufferedImage(44, 44, BufferedImage.TYPE_INT_ARGB);
        for(int y = 0; y < 44; y++) {
            int width;
            if(y < 22) {
                width = 2 * (y + 1);
            } else {
                width = 2 * (44 - y);
            }
            for(int x = 0; x < 44; x++) {
                if(x < 22 - width / 2) {
                    entry.image.setRGB(x, y, 0);
                } else if (x >= 22 - width / 2 && x < 22 + width / 2) {
                    entry.image.setRGB(x, y, SLColor.convert555(artData.readUWord(), 0xFF));
                } else {
                    entry.image.setRGB(x, y, 0);
                }
            }
        }
        return entry;
    }

    public synchronized ArtEntry getStaticArt(int staticID, boolean translucent) {
        Integer offset = getArtFileOffset(staticID + 0x4000);
        if(offset == null) {
            return null;
        }
        artData.seek(offset);
        ArtEntry entry = new ArtEntry();
        entry.unknown = artIdx.readUDWord();
        entry.id = staticID;

        int alpha = 0xFF;
        if(translucent){
            alpha = 0xC0;
        }

        artData.readUDWord(); // unknown, probably size of encoded data
        int width = artData.readUWord();
        int height = artData.readUWord();

        if(width == 0 || height == 0) {
            entry.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        } else {
            entry.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int[] rowStarts = new int[height];
            for(int i = 0; i < height; i++) {
                rowStarts[i] = artData.readUWord();
            }
            offset = artData.getPosition();
            for(int y = 0; y < height; y++) {
                int x = 0;
                artData.seek(offset + rowStarts[y] * 2);
                int numTransparent = artData.readUWord();
                int numVisible = artData.readUWord();

                // transparent start
                x += numTransparent;

                // real pixels
                for(int i = 0; i < numVisible; i++) {
                    int color = artData.readUWord();
                    if(color != 0) { // 0 means transparent here
                        entry.image.setRGB(x, y, SLColor.convert555(color, alpha));
                    }
                    x++;
                }

                // transparent end -> nothing to do
            }
        }

        return entry;
    }

    public synchronized ItemAnimation getStaticAnimation(int staticID) {
        ItemAnimation res = new ItemAnimation();
        int block = staticID / 8;
        int entry = staticID % 8;
        int offset = block * (4 + 8 * 20) + 4 + entry * 20;
        animData.seek(offset);
        res.frameOffsets = animData.readRaw(16);
        res.currentIndex = animData.readUByte();
        res.maxIndex = animData.readUByte();
        res.unknown1 = animData.readUByte();
        res.unknown2 = animData.readUByte();
        return res;
    }

    /*
     * There are only 5 images for 8 directions, i.e. some frames have to be mirrored according to the facing table:
     *  facingTable = [3, 2, 1, 0, 1, 2, 3, 4]
     * When fighting, a timer increases frame indices from 0 to 6 (inclusive) instead of using the step count
     *
     * If mobile.graphic < 0x31
     *  when not fighting:
     *    staticId = 0x8083 + (graphic * 20 + facingTable[facing]) * 10 + (stepCounter % 9)
     *    that's 45 frames per graphic
     *  when fighting:
     *    staticId = 0x8001 + graphic * 200 + unknown1 * facingtable[facing] + unknown2 + fightFrameIndex
     *    unknown1 can be 6 or 3
     *
     * Else
     *  when not fighting:
     *   staticId = 0x8001 + (graphic * 40 + facingTable[facing]) * 5  + (stepCounter % 4)
     *  when fighting:
     *   staticId = 0x8001 + graphic * 200 + unknown1 * facingTable[facing] + unknown2 + fightFrameIndex
     *    that's 15 frames per graphic
     */

    public MobileAnimation getAnimationEntry(int mobileID, Direction facing, boolean isFighting) {
        MobileAnimation res = new MobileAnimation();
        res.frames = new ArrayList<>();

        if(facing == Direction.NORTH || facing == Direction.NORTH_EAST || facing == Direction.EAST) {
            res.needMirror = true;
        }

        if(mobileID < 0x31) {
            if(isFighting) {
                res.id = 0x8001 + mobileID * 200 + 6 * facing.getFrameIndex();
                for(int i = 0; i < 6; i++) {
                    ArtEntry entry = getStaticArt(res.id - 0x4000 + i, false);
                    if(entry == null) {
                        continue;
                    }
                    res.frames.add(res.id - 0x4000 + i);
                }
            } else {
                res.id = 0x8083 + (20 * mobileID + facing.getFrameIndex()) * 10;
                for(int i = 0; i < 9; i++) {
                    ArtEntry entry = getStaticArt(res.id - 0x4000 + i, false);
                    if(entry == null) {
                        continue;
                    }
                    res.frames.add(res.id - 0x4000 + i);
                }
            }
        } else {
            if(isFighting) {
                // no special fighting graphic for these mobiles
                return null;
            }
            res.id = 0x8001 + (40 * mobileID + facing.getFrameIndex()) * 5;
            for(int i = 0; i < 5; i++) {
                ArtEntry entry = getStaticArt(res.id - 0x4000 + i, false);
                if(entry == null) {
                    continue;
                }
                res.frames.add(res.id - 0x4000 + i);
            }
        }

        if(res.frames.size() == 0) {
            return null;
        }

        return res;
    }
}
