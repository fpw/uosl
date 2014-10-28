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

import org.solhost.folko.uosl.libuosl.network.ItemStub;
import org.solhost.folko.uosl.libuosl.network.MobileStub;
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.network.SendableMobile;

public class EquipPacket extends SLPacket {
    public static final short ID = 0x4D;
    private SendableMobile mobile;
    private SendableItem item;

    public EquipPacket(SendableMobile mob, SendableItem item) {
        initWrite(ID, 0x12);
        addUDWord(item.getSerial());
        addUWord(item.getGraphic());
        addUByte((short) 0); // unknown
        addUByte(item.getLayer());
        addUDWord(mob.getSerial());
        addUWord(item.getHue());
    }

    private EquipPacket() {
    }

    public static EquipPacket read(ByteBuffer buffer, int length) {
        EquipPacket res = new EquipPacket();
        ItemStub item = new ItemStub();
        MobileStub mobile = new MobileStub();

        item.setSerial(readUDWord(buffer));
        item.setGraphic(readUWord(buffer));
        readUByte(buffer);
        item.setLayer(readUByte(buffer));
        mobile.setSerial(readUDWord(buffer));
        item.setHue(readUWord(buffer));

        res.item = item;
        res.mobile = mobile;
        return res;
    }

    public SendableMobile getMobile() {
        return mobile;
    }

    public SendableItem getItem() {
        return item;
    }

    @Override
    public short getID() {
        return ID;
    }
}
