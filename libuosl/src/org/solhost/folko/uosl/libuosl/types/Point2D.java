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

import java.io.Serializable;

public class Point2D implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAP_WIDTH = 1024;
    public static final int MAP_HEIGHT = 1024;
    protected final int x, y;

    public Point2D(int x, int y) {
        if(x < 0 || x > MAP_WIDTH) {
            throw new IllegalArgumentException("invalid x coordinate: " + x);
        }
        this.x = x;

        if(y < 0 || y > MAP_HEIGHT) {
            throw new IllegalArgumentException("invalid y coordinate: " + y);
        }
        this.y = y;
    }

    public int distanceTo(Point2D other) {
        return l2DistanceTo(other);
    }

    // maximum norm
    public int lMaxDistanceTo(Point2D other) {
        int dx = Math.abs(x - other.x);
        int dy = Math.abs(y - other.y);
        return Math.max(dx, dy);
    }

    // L1 norm
    public int l1DistanceTo(Point2D other) {
        int dx = Math.abs(x - other.x);
        int dy = Math.abs(y - other.y);
        return dx + dy;
    }

    // L2 norm
    public int l2DistanceTo(Point2D other) {
        int dx = x - other.x;
        int dy = y - other.y;
        return (int) Math.round(Math.sqrt(dx * dx + dy * dy));
    }

    public Direction getDirectionTo(Point2D other) {
        int dx = getX() - other.getX();
        int dy = getY() - other.getY();

        if(dx > 0) {
            if     (dy  > 0) return Direction.NORTH_WEST;
            else if(dy == 0) return Direction.WEST;
            else if(dy  < 0) return Direction.SOUTH_WEST;
        } if(dx == 0) {
            if     (dy  > 0) return Direction.NORTH;
            else if(dy  < 0) return Direction.SOUTH;
        } else if(dx < 0){
            if     (dy  > 0) return Direction.NORTH_EAST;
            else if(dy == 0) return Direction.EAST;
            else if(dy  < 0) return Direction.SOUTH_EAST;
        }
        return Direction.NORTH; // identical locations
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point2D getTranslated(Direction dir) {
        int newX = getX();
        int newY = getY();
        switch(dir) {
        case NORTH:                 newY--; break;
        case NORTH_EAST:    newX++; newY--; break;
        case EAST:          newX++;         break;
        case SOUTH_EAST:    newX++; newY++; break;
        case SOUTH:                 newY++; break;
        case SOUTH_WEST:    newX--; newY++; break;
        case WEST:          newX--;         break;
        case NORTH_WEST:    newX--; newY--; break;
        }
        if(newX < 0) newX = 0;
        if(newX >= MAP_WIDTH) newX = MAP_WIDTH - 1;
        if(newY < 0) newY = 0;
        if(newY >= MAP_HEIGHT) newY = MAP_HEIGHT - 1;
        return new Point2D(newX, newY);
    }

    public int getCellIndex() {
        return (x / 8) * (MAP_WIDTH / 8) + (y / 8);
    }

    public int getTileIndex() {
        return (x % 8) + ((y % 8) * 8);
    }

    public static Point2D fromCell(int cell, int xOff, int yOff) {
        int x = (cell / (MAP_WIDTH / 8) * 8) + xOff;
        int y = (cell % (MAP_WIDTH / 8) * 8) + yOff;
        return new Point2D(x, y);
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Point2D))
            return false;
        Point2D other = (Point2D) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("<Point2D: x = %d, y = %d>", x, y);
    }
}
