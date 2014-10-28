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

import org.solhost.folko.uosl.libuosl.network.SendableMobile;

public class ShopPacket extends SLPacket {
    public static final short ID = 0x70;
    private long shopSerial;
    private short action;

    private ShopPacket() {
    }

    // sending
    public ShopPacket(SendableMobile shop, short action) {
        initWrite(ID, 0x09);
        addUDWord(shop.getSerial());
        addUByte(action);

        this.shopSerial = shop.getSerial();
        this.action = action;
    }

    // receiving
    public static ShopPacket read(ByteBuffer b, int len) {
        ShopPacket res = new ShopPacket();
        res.shopSerial = readUDWord(b);
        res.action = readUByte(b);
        return res;
    }

    public short getAction() {
        return action;
    }

    public long getShopSerial() {
        return shopSerial;
    }

    @Override
    public short getID() {
        return ID;
    }
}
