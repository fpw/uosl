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

public class SLTiles {
    public static final int NUM_TEXTURES = 16384;
    public static final int NUM_STATICS = 16384;
    private static final int STATIC_START = 428032;
    private final SLDataFile tileDataFile;

    public class LandTile {
        public static final long FLAG_IMPASSABLE    = 0x00000040;

        public long flags;
        public int textureID;
        public String name;

        public boolean isImpassable() {
            return (flags & FLAG_IMPASSABLE) != 0;
        }
    }

    public class StaticTile {
        public static final long FLAG_BACKGROUND    = 0x00000001;
        public static final long FLAG_WEAPON        = 0x00000002;
        public static final long FLAG_TRANSPARENT   = 0x00000004;
        public static final long FLAG_TRANSLUCENT   = 0x00000008;
        public static final long FLAG_IMPASSABLE    = 0x00000040;
        public static final long FLAG_SURFACE       = 0x00000200;
        public static final long FLAG_STAIRS        = 0x00000400;
        public static final long FLAG_STACKABLE     = 0x00000800;
        public static final long FLAG_WINDOW        = 0x00001000;
        public static final long FLAG_NO_SHOOT      = 0x00002000;
        public static final long FLAG_ARTICLE_A     = 0x00004000;
        public static final long FLAG_ARTICLE_AN    = 0x00008000;
        public static final long FLAG_GENERATOR     = 0x00010000;
        public static final long FLAG_FOLIAGE       = 0x00020000;
        public static final long FLAG_CONTAINER     = 0x00200000;
        public static final long FLAG_EQUIPABLE     = 0x00400000;
        public static final long FLAG_LIGHTSOURCE   = 0x00800000;
        public static final long FLAG_ARMOR         = 0x08000000;
        public static final long FLAG_ROOF          = 0x10000000;
        public static final long FLAG_DOOR          = 0x20000000;

        public static final short LAYER_WEAPON      = 0x01;
        public static final short LAYER_SHIELD      = 0x02;
        public static final short LAYER_BRACES      = 0x03;
        public static final short LAYER_LEG         = 0x04;
        public static final short LAYER_BREAST      = 0x05;
        public static final short LAYER_HEAD        = 0x06;
        public static final short LAYER_BACKPACK    = 0x09;
        public static final short LAYER_NECK        = 0x0A;
        public static final short LAYER_HAIR        = 0x0B;
        public static final short LAYER_SKIRT       = 0x0C;

        public long flags;
        public short weight;
        public short layer;
        public short height;
        public int animationID;
        public String name;
        public int unknown1, unknown2, price;

        public boolean isContainer() {
            return (flags & FLAG_CONTAINER) != 0;
        }

        public boolean isSurface() {
            return (flags & FLAG_SURFACE) != 0;
        }

        public boolean isWearable() {
            return (flags & FLAG_EQUIPABLE) != 0;
        }

        public boolean isImpassable() {
            return (flags & FLAG_IMPASSABLE) != 0;
        }

        public boolean isStair() {
            return (flags & FLAG_STAIRS) != 0;
        }

        public boolean isStackable() {
            return (flags & FLAG_STACKABLE) != 0;
        }
    }

    public SLTiles(String tilePath) throws IOException {
        tileDataFile = new SLDataFile(tilePath, true);
    }

    public synchronized LandTile getLandTile(int textureID) {
        LandTile res = new LandTile();
        if(textureID < 0 || textureID > NUM_TEXTURES) {
            throw new IllegalArgumentException("invalid texture ID: " + textureID);
        }
        int landGroupIndex = textureID / 32;
        int landGroupOffset = textureID % 32;

        int offset = landGroupIndex * 836 + 4 + landGroupOffset * 26;
        tileDataFile.seek(offset);
        res.flags = tileDataFile.readUDWord();
        res.textureID = tileDataFile.readUWord();
        res.name = tileDataFile.readString();
        return res;
    }

    public synchronized StaticTile getStaticTile(int staticID) {
        StaticTile res = new StaticTile();
        if(staticID < 0 || staticID > NUM_STATICS) {
            throw new IllegalArgumentException("invalid static ID: " + staticID);
        }
        int staticGroupIndex = staticID / 32;
        int staticGroupOffset = staticID % 32;
        int offset = STATIC_START + staticGroupIndex * 1188 + 4 + staticGroupOffset * 37;
        tileDataFile.seek(offset);
        res.flags = tileDataFile.readUDWord();
        res.weight = tileDataFile.readUByte();
        res.layer = tileDataFile.readUByte();
        res.unknown1 = tileDataFile.readUWord();
        tileDataFile.readUByte(); // unused
        tileDataFile.readUByte(); // unused
        res.animationID = tileDataFile.readUWord();
        res.unknown2 = tileDataFile.readUByte();
        tileDataFile.readUByte(); // unused
        res.price = tileDataFile.readUWord();
        res.height = tileDataFile.readUByte();
        res.name = tileDataFile.readString();
        return res;
    }

    public String getTextureName(int textureID) {
        LandTile tile = getLandTile(textureID);
        return tile.name;
    }

    public String getStaticName(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return tile.name;
    }

    public int getStaticHeight(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return tile.height;
    }

    public short getEquipmentLayer(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return tile.layer;
    }

    public short getWeight(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return tile.weight;
    }

    public int getPrice(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return tile.price;
    }

    public boolean isContainer(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return (tile.flags & StaticTile.FLAG_CONTAINER) != 0;
    }

    public boolean isStackable(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return (tile.flags & StaticTile.FLAG_STACKABLE) != 0;
    }

    public boolean isWearable(int staticID) {
        StaticTile tile = getStaticTile(staticID);
        return (tile.flags & StaticTile.FLAG_EQUIPABLE) != 0;
    }
}
