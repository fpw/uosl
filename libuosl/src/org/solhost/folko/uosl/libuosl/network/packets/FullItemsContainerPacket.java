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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.solhost.folko.uosl.libuosl.network.ItemStub;
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class FullItemsContainerPacket extends SLPacket {
    public static final short ID = 0x71;
    private List<SendableItem> items;
    private long containerSerial;

    private FullItemsContainerPacket() {
        items = new ArrayList<SendableItem>();
    }

    public static FullItemsContainerPacket read(ByteBuffer b, int len) {
        FullItemsContainerPacket res = new FullItemsContainerPacket();
        int itemCount = readUWord(b);
        for(int i = 0; i < itemCount; i++) {
            ItemStub itm = new ItemStub();
            itm.setSerial(readUDWord(b));
            itm.setGraphic(readUWord(b));
            readUByte(b);
            itm.setAmount(readUWord(b));
            int x = readUWord(b);
            int y = readUWord(b);
            itm.setLocation(new Point3D(x, y));
            res.containerSerial = readUDWord(b); // TODO: restricted to one container per packet now
            itm.setHue(readUWord(b));
            res.items.add(itm);
        }
        return res;
    }

    public FullItemsContainerPacket(Collection<? extends SendableItem> items, SendableItem container) {
        initWrite(ID, 4 + 2 + items.size() * 19);
        addUWord(items.size());
        for(SendableItem item : items) {
            addUDWord(item.getSerial());
            addUWord(item.getGraphic());
            addUByte((short) 0); // seems to be added to item graphic if container is a container
            addUWord(item.getAmount());
            addUWord(item.getLocation().getX());
            addUWord(item.getLocation().getY());
            addUDWord(container.getSerial());
            addUWord(item.getHue());
        }
    }

    public List<SendableItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public long getContainerSerial() {
        return containerSerial;
    }

    @Override
    public short getID() {
        return ID;
    }
}
