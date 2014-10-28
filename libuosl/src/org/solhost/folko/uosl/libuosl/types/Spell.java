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

public enum Spell {
    LIGHTSOURCE, DARKSOURCE, GREATLIGHT, LIGHT, HEALING, FIREBALL, CREATEFOOD;

    public static Spell fromScrollGraphic(int scrollGraphic) {
        switch(scrollGraphic) {
        case Items.GFX_SCROLL_LIGHTSOURCE:   return LIGHTSOURCE;
        case Items.GFX_SCROLL_DARKSOURCE:    return DARKSOURCE;
        case Items.GFX_SCROLL_GREATLIGHT:    return GREATLIGHT;
        case Items.GFX_SCROLL_LIGHT:         return LIGHT;
        case Items.GFX_SCROLL_HEALING:       return HEALING;
        case Items.GFX_SCROLL_FIREBALL:      return FIREBALL;
        case Items.GFX_SCROLL_CREATEFOOD:    return CREATEFOOD;
        default: return null;
        }
    }

    public int toScrollType() {
        switch(this) {
            case LIGHTSOURCE:   return Items.GFX_SCROLL_LIGHTSOURCE;
            case DARKSOURCE:    return Items.GFX_SCROLL_DARKSOURCE;
            case GREATLIGHT:    return Items.GFX_SCROLL_GREATLIGHT;
            case LIGHT:         return Items.GFX_SCROLL_LIGHT;
            case HEALING:       return Items.GFX_SCROLL_HEALING;
            case FIREBALL:      return Items.GFX_SCROLL_FIREBALL;
            case CREATEFOOD:    return Items.GFX_SCROLL_CREATEFOOD;
            default: return 0;
        }
    }

    public String getPowerWords() {
        switch(this) {
        case LIGHTSOURCE:       return "Quas In Lor";
        case DARKSOURCE:        return "Quas An Lor";
        case GREATLIGHT:        return "In Vas Lor";
        case LIGHT:             return "In Lor";
        case HEALING:           return "Mani";
        case FIREBALL:          return "Por Flam";
        case CREATEFOOD:        return "In Mani Ylem";
        default:                return "";
        }
    }

    public short toByte() {
        switch(this) {
        case LIGHTSOURCE:   return 0;
        case DARKSOURCE:    return 1;
        case GREATLIGHT:    return 2;
        case LIGHT:         return 3;
        case HEALING:       return 4;
        case FIREBALL:      return 5;
        case CREATEFOOD:    return 6;
        default: throw new RuntimeException("Invalid spell");
        }
    }
}
