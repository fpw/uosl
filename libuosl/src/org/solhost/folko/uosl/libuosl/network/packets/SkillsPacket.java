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
package org.solhost.folko.uosl.libuosl.network.packets;

import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.types.Attribute;

public class SkillsPacket extends SLPacket {
    public static final short ID = 0x6F;

    public SkillsPacket(SendableMobile mob, boolean openSkillWindow) {
        initWrite(ID, 25);
        addUByte((short) (openSkillWindow ? 0 : 1));
        addUWord((int) mob.getAttribute(Attribute.MAGIC_DEFENSE));
        addUWord((int) mob.getAttribute(Attribute.BATTLE_DEFENSE));
        addUWord((int) mob.getAttribute(Attribute.STEALING));
        addUWord((int) mob.getAttribute(Attribute.HIDING));
        addUWord((int) mob.getAttribute(Attribute.FIRST_AID));
        addUWord((int) mob.getAttribute(Attribute.DETECT_TRAP));
        addUWord((int) mob.getAttribute(Attribute.PEEK));
        addUWord((int) mob.getAttribute(Attribute.MAGIC));
        addUWord((int) mob.getAttribute(Attribute.MELEE));
        addUWord((int) mob.getAttribute(Attribute.RANGED_WEAPONS));
    }

    @Override
    public short getID() {
        return ID;
    }
}
