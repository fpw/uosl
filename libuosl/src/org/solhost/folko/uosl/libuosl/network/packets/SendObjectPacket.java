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

import org.solhost.folko.uosl.libuosl.network.ObjectStub;
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.network.SendableObject;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class SendObjectPacket extends SLPacket {
    public static final short ID = 0x35;
    private SendableObject object;
    private int amount;
    private Direction facing;

    public SendObjectPacket(SendableObject obj) {
        initWrite(ID, 0x15);
        addUDWord(obj.getSerial());
        addUWord(obj.getGraphic());
        addUByte((short) 0); // unknown, seems to be added to graphic
        if(obj instanceof SendableItem) {
            addUWord(((SendableItem) obj).getAmount());
        } else {
            addUWord(0);
        }
        addUWord(obj.getLocation().getX());
        addUWord(obj.getLocation().getY());
        if(obj instanceof SendableMobile) {
            addUByte(((SendableMobile) obj).getFacing().toByte());
        } else if(obj instanceof SendableItem){
            addUByte(((SendableItem) obj).getFacingOverride());
        } else {
            addUByte((short) 0);
        }
        addSByte((byte) obj.getLocation().getZ());
        addUWord(obj.getHue());
    }

    private SendObjectPacket() {
    }

    public static SendObjectPacket read(ByteBuffer buffer, int length) {
        SendObjectPacket res = new SendObjectPacket();
        ObjectStub object = new ObjectStub();

        object.setSerial(readUDWord(buffer));
        object.setGraphic(readUWord(buffer));
        readUByte(buffer);
        res.amount = readUWord(buffer);
        int x = readUWord(buffer);
        int y = readUWord(buffer);
        res.facing = Direction.parse(readUByte(buffer));
        byte z = readSByte(buffer);
        object.setLocation(new Point3D(x, y, z));
        object.setHue(readUWord(buffer));

        res.object = object;
        return res;
    }

    public SendableObject getObject() {
        return object;
    }

    public Direction getFacing() {
        return facing;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public short getID() {
        return ID;
    }
}
