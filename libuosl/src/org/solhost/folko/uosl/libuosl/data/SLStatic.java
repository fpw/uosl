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
package org.solhost.folko.uosl.libuosl.data;

import org.solhost.folko.uosl.libuosl.types.Point3D;

public class SLStatic {
    private final long serial;
    private final int staticID;
    private final int hue;
    private final Point3D location;

    public SLStatic(long serial, int staticID, Point3D location, int hue) {
        this.serial = serial;
        this.staticID = staticID;
        this.hue = hue;
        this.location = location;
    }

    public long getSerial() {
        return serial;
    }

    public int getStaticID() {
        return staticID;
    }

    public int getHue() {
        return hue;
    }

    public Point3D getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("<SLStatic: serial = %08X, static = %04X, hue = %04X, position = %s>",
                serial, staticID, hue, location.toString());
    }
}
