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
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class ItemInContainerPacket extends SLPacket {
    public static final short ID = 0x43;
    private SendableItem item;
    private long containerSerial;

    private ItemInContainerPacket() {
    }

    public static ItemInContainerPacket read(ByteBuffer b, int len) {
        ItemInContainerPacket res = new ItemInContainerPacket();
        ItemStub itm = new ItemStub();

        itm.setSerial(readUDWord(b));
        itm.setGraphic(readUWord(b));
        readUByte(b);
        itm.setAmount(readUWord(b));
        int x = readUWord(b);
        int y = readUWord(b);
        itm.setLocation(new Point3D(x, y));
        res.containerSerial = readUDWord(b);
        itm.setHue(readUWord(b));

        res.item = itm;
        return res;
    }

    public ItemInContainerPacket(SendableItem item, SendableItem container) {
        initWrite(ID, 0x17);
        addUDWord(item.getSerial());
        addUWord(item.getGraphic());
        addUByte((short) 0); // unknown
        addUWord(item.getAmount());
        addUWord(item.getLocation().getX());
        addUWord(item.getLocation().getY());
        addUDWord(container.getSerial());
        addUWord(item.getHue());
    }

    public SendableItem getItem() {
        return item;
    }

    public long getContainerSerial() {
        return containerSerial;
    }

    @Override
    public short getID() {
        return ID;
    }
}
