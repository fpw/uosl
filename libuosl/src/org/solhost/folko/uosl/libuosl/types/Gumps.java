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
package org.solhost.folko.uosl.libuosl.types;

import java.awt.Rectangle;

public class Gumps {
    public static final int ID_CORPSE      = 0x0009;
    public static final int ID_PAPERDOLL   = 0x000A;
    public static final int ID_SHOP        = 0x0030;
    public static final int ID_BACKPACK    = 0x003C;
    public static final int ID_BAG         = 0x003D;
    public static final int ID_BARREL      = 0x003E;
    public static final int ID_BOX         = 0x003F;
    public static final int ID_SMALL_BOX   = 0x0040;
    public static final int ID_BASKET      = 0x0041;
    public static final int ID_METAL_CHEST = 0x0042;
    public static final int ID_WOOD_CHEST  = 0x0043;
    public static final int ID_CRATE       = 0x0044;
    public static final int ID_SPELLBOOK   = 0xFFFF;

    public static Rectangle getGumpDimensions(int gumpID) {
        switch(gumpID) {
        case ID_BACKPACK:   return new Rectangle(44, 65, 98, 85);
        case ID_BAG:        return new Rectangle(29, 34, 64, 85);
        case ID_BASKET:     return new Rectangle(35, 38, 66, 69);
        case ID_BOX:        return new Rectangle(16, 45, 92, 71);
        case ID_SMALL_BOX:  return new Rectangle(19, 47, 106, 67);
        case ID_METAL_CHEST:return new Rectangle(14, 105, 108, 68);
        case ID_WOOD_CHEST: return new Rectangle(16, 51, 133, 65);
        case ID_BARREL:     return new Rectangle(33, 36, 65, 103);
        case ID_CORPSE:     return new Rectangle(4, 85, 89, 80);
        default: return new Rectangle(50, 60, 20, 20);
        }
    }

    public static boolean isContainerGump(int gumpID) {
        switch(gumpID) {
        case ID_BACKPACK:   return true;
        case ID_BAG:        return true;
        case ID_BASKET:     return true;
        case ID_BOX:        return true;
        case ID_SMALL_BOX:  return true;
        case ID_METAL_CHEST:return true;
        case ID_WOOD_CHEST: return true;
        case ID_BARREL:     return true;
        case ID_CORPSE:     return true;
        default: return false;
        }
    }

    public static int getItemGump(int graphicID) {
        switch(graphicID) {
        case Items.GFX_BACKPACK:        return ID_BACKPACK;
        case Items.GFX_BAG:             return ID_BAG;
        case Items.GFX_BASKET:          return ID_BASKET;
        case Items.GFX_BOX:             return ID_BOX;
        case Items.GFX_SMALL_BOX:       return ID_SMALL_BOX;
        case Items.GFX_BUSHEL_BASKET:   return ID_BASKET;
        case Items.GFX_METAL_CHEST:     return ID_METAL_CHEST;
        case Items.GFX_WOOD_CHEST:      return ID_WOOD_CHEST;
        case Items.GFX_BARREL:          return ID_BARREL;

        case Items.GFX_CORPSE_DEER:
        case Items.GFX_CORPSE_HUMAN:
        case Items.GFX_CORPSE_ORC:
        case Items.GFX_CORPSE_ORC_CAPTAIN:
        case Items.GFX_CORPSE_RABBIT:
        case Items.GFX_CORPSE_SKELETON:
        case Items.GFX_CORPSE_WOLF:
                                        return ID_CORPSE;

        case Items.GFX_SPELLBOOK:       return ID_SPELLBOOK;

        default:                        return ID_BACKPACK;
        }
    }
}
