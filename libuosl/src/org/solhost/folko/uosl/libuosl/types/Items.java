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

public class Items {
    // Hardcoded into client: serials below are considered mobiles
    public static final long SERIAL_FIRST           = 0x40000000;
    public static final long SERIAL_LAST            = 0x7FFFFFFF;

    // Containers
    public static final int GFX_BACKPACK            = 0x0348;
    public static final int GFX_BAG                 = 0x0349;
    public static final int GFX_BASKET              = 0x034A;
    public static final int GFX_BOX                 = 0x034B;
    public static final int GFX_SMALL_BOX           = 0x034C;
    public static final int GFX_BUSHEL_BASKET       = 0x034D;
    public static final int GFX_METAL_CHEST         = 0x034E;
    public static final int GFX_WOOD_CHEST          = 0x034F;
    public static final int GFX_SHOP_CONTAINER      = 0x00A7;
    public static final int GFX_INVIS_PACK          = 0x01B7;
    public static final int GFX_BARREL              = 0x01A3;

    // Magery
    public static final int GFX_SPELLBOOK           = 0x0386;
    public static final int GFX_SCROLL_LIGHTSOURCE  = 0x0387;
    public static final int GFX_SCROLL_DARKSOURCE   = 0x0444;
    public static final int GFX_SCROLL_GREATLIGHT   = 0x0445;
    public static final int GFX_SCROLL_LIGHT        = 0x0446;
    public static final int GFX_SCROLL_HEALING      = 0x0447;
    public static final int GFX_SCROLL_FIREBALL     = 0x0448;
    public static final int GFX_SCROLL_CREATEFOOD   = 0x0449;
    public static final int GFX_DARKSOURCE          = 0x01B2;
    public static final int GFX_LIGHTSOURCE         = 0x01B3;

    // Clothes
    public static final int GFX_TUNIC               = 0x02F1;
    public static final int GFX_PANTS               = 0x02F2;
    public static final int GFX_SKIRT               = 0x02F3;
    public static final int GFX_HAIR_START          = 0x0424;
    public static final int GFX_HAIR_END            = 0x0429;

    // Weapons
    public static final int GFX_GREAT_AXE           = 0x0293;
    public static final int GFX_EXECUTIONER_AXE     = 0x0294;
    public static final int GFX_HAND_AXE            = 0x0296;
    public static final int GFX_BATTLE_AXE          = 0x0298;
    public static final int GFX_WAR_AXE             = 0x029A;
    public static final int GFX_DAGGER              = 0x02A1;
    public static final int GFX_MACE                = 0x02A2;
    public static final int GFX_SHORT_SWORD         = 0x02A4;
    public static final int GFX_BROAD_SWORD         = 0x02A6;

    // Armor
    public static final int GFX_VAMBRACES           = 0x02EC;
    public static final int GFX_BREASTPLATE         = 0x02ED;
    public static final int GFX_GORGET              = 0x02EE;
    public static final int GFX_HELMET              = 0x02EF;
    public static final int GFX_GREAVES             = 0x02F0;
    public static final int GFX_HEATER              = 0x3D58;
    public static final int GFX_BRACERS             = 0x0442;
    public static final int GFX_JERKIN              = 0x0441;
    public static final int GFX_LEGGINGS            = 0x0443;
    public static final int GFX_LEATHER_CAP         = 0x044A;

    // Corpses
    public static final int GFX_CORPSE_HUMAN        = 0x3D67;
    public static final int GFX_CORPSE_SKELETON     = 0x3D66;
    public static final int GFX_CORPSE_DEER         = 0x3D69;
    public static final int GFX_CORPSE_ORC          = 0x3D65;
    public static final int GFX_CORPSE_ORC_CAPTAIN  = 0x3D64;
    public static final int GFX_CORPSE_RABBIT       = 0x3D6B;
    public static final int GFX_CORPSE_WOLF         = 0x3D6A;

    // Misc
    public static final int GFX_GOLD                = 0x01F8;
    public static final int GFX_SEXTANT             = 0x0461;
    public static final int GFX_POTION_YELLOW       = 0x0383;
}
