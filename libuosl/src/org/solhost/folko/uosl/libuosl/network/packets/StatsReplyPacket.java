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

public class StatsReplyPacket extends SLPacket {
    public static final short ID = 0x33;

    // Send someone else's stats
    public StatsReplyPacket(SendableMobile mob, int hits, int maxHits) {
        initWrite(ID, 0x2B);
        addUDWord(mob.getSerial());
        addString(mob.getName(), 30);
        addUWord(hits);
        addUWord(maxHits);
        addUByte((short) 0); // unextended info
    }

    // Send own stats
    public StatsReplyPacket(SendableMobile mob) {
        initWrite(ID, 0x44);
        addUDWord(mob.getSerial());
        addString(mob.getName(), 30);
        addUWord((int) mob.getAttribute(Attribute.HITS));
        addUWord((int) mob.getAttribute(Attribute.MAX_HITS));
        addUByte((short) 1); // extended info
        addUByte((short) mob.getGraphic());
        addUWord((int) mob.getAttribute(Attribute.STRENGTH));
        addUWord((int) mob.getAttribute(Attribute.DEXTERITY));
        addUWord((int) mob.getAttribute(Attribute.INTELLIGENCE));
        addUWord((int) mob.getAttribute(Attribute.FATIGUE));
        addUWord((int) mob.getAttribute(Attribute.MAX_FATIGUE));
        addUWord((int) mob.getAttribute(Attribute.MANA));
        addUWord((int) mob.getAttribute(Attribute.MAX_MANA));
        addUDWord(mob.getAttribute(Attribute.EXPERIENCE));
        addUDWord(mob.getAttribute(Attribute.NEXT_LEVEL));
        addUWord((int) mob.getAttribute(Attribute.LEVEL) - 1);
    }

    @Override
    public short getID() {
        return ID;
    }

}
