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

import java.nio.ByteBuffer;

import org.solhost.folko.uosl.libuosl.network.MobileStub;
import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.types.Attribute;

public class StatsUpdatePacket extends SLPacket {
    public static final short ID = 0x4C;
    private SendableMobile mobile;

    public StatsUpdatePacket(SendableMobile mob, boolean relativeHitsOnly) {
        initWrite(ID, 0x14);

        addUDWord(mob.getSerial());

        int hits = (int) mob.getAttribute(Attribute.HITS),
            maxHits = (int) mob.getAttribute(Attribute.MAX_HITS);

        if(relativeHitsOnly) {
            hits = hits * 100 / maxHits;
            maxHits = 100;
        }

        addUWord(maxHits);
        addUWord(hits);
        if(relativeHitsOnly) {
            addUWord(0);
            addUWord(0);
            addUWord(0);
            addUWord(0);
        } else {
            addUWord((int) mob.getAttribute(Attribute.MAX_MANA));
            addUWord((int) mob.getAttribute(Attribute.MANA));
            addUWord((int) mob.getAttribute(Attribute.MAX_FATIGUE));
            addUWord((int) mob.getAttribute(Attribute.FATIGUE)); //needs to be > 5 or client won't be able to walk
        }
    }

    private StatsUpdatePacket() {
    }

    public static StatsUpdatePacket read(ByteBuffer buffer, int lenght) {
        StatsUpdatePacket res = new StatsUpdatePacket();
        MobileStub m = new MobileStub();

        m.setSerial(readUDWord(buffer));
        m.setAttribute(Attribute.MAX_HITS,      readUWord(buffer));
        m.setAttribute(Attribute.HITS,          readUWord(buffer));
        m.setAttribute(Attribute.MAX_MANA,      readUWord(buffer));
        m.setAttribute(Attribute.MANA,          readUWord(buffer));
        m.setAttribute(Attribute.MAX_FATIGUE,   readUWord(buffer));
        m.setAttribute(Attribute.FATIGUE,       readUWord(buffer));

        res.mobile = m;
        return res;
    }

    public SendableMobile getMobile() {
        return mobile;
    }

    @Override
    public short getID() {
        return ID;
    }
}
