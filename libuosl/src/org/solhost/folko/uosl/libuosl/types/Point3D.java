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

public class Point3D extends Point2D {
    private static final long serialVersionUID = 1L;
    public static final int MIN_ELEVATION = -128;
    public static final int MAX_ELEVATION = 127;
    protected final int z;

    public Point3D(int x, int y) {
        super(x, y);
        this.z = 0;
    }

    public Point3D(int x, int y, int z) {
        super(x, y);
        if(z < MIN_ELEVATION || z > MAX_ELEVATION) {
            throw new IllegalArgumentException("invalid z coordinate: " + z);
        }
        this.z = z;
    }

    public Point3D(Point2D d2, int z) {
        super(d2.x, d2.y);
        if(z < MIN_ELEVATION || z > MAX_ELEVATION) {
            throw new IllegalArgumentException("invalid z coordinate: " + z);
        }
        this.z = z;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Point3D))
            return false;
        Point3D other = (Point3D) obj;
        if (z != other.z)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("<Point3D: x = %d, y = %d, z = %d>", x, y, z);
    }
}
