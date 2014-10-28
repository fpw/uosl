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

public enum Attribute {
    // Stats
    STRENGTH, DEXTERITY, INTELLIGENCE,
    HITS, FATIGUE, MANA,
    // Skills
    MAGIC_DEFENSE, BATTLE_DEFENSE,
    STEALING, HIDING, PEEK,
    FIRST_AID, DETECT_TRAP,
    MAGIC, MELEE, RANGED_WEAPONS,
    // Level
    EXPERIENCE, LEVEL,
    // Dynamically calculated
    MAX_HITS, MAX_MANA, MAX_FATIGUE, NEXT_LEVEL;

    public boolean isSkill() {
        switch(this) {
        case MAGIC_DEFENSE:
        case BATTLE_DEFENSE:
        case STEALING:
        case HIDING:
        case PEEK:
        case FIRST_AID:
        case DETECT_TRAP:
        case MAGIC:
        case MELEE:
        case RANGED_WEAPONS:
            return true;
        default:
            return false;
        }
    }

    public boolean isBasicStat() {
        switch(this) {
        case STRENGTH:
        case DEXTERITY:
        case INTELLIGENCE:
        case EXPERIENCE:
        case LEVEL:
        case NEXT_LEVEL:
            return true;
        default:
            return false;
        }
    }

    public boolean isDynamicStat() {
        switch(this) {
        case HITS:
        case FATIGUE:
        case MANA:
        case MAX_HITS:
        case MAX_MANA:
        case MAX_FATIGUE:
            return true;
        default:
            return false;
        }
    }
}
