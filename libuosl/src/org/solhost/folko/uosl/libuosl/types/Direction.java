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

public enum Direction {
    NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;

    public static Direction parse(short b) {
        switch(b) {
        case 0: return NORTH;
        case 1: return NORTH_EAST;
        case 2: return EAST;
        case 3: return SOUTH_EAST;
        case 4: return SOUTH;
        case 5: return SOUTH_WEST;
        case 6: return WEST;
        case 7: return NORTH_WEST;
        default: return NORTH;
        }
    }

    public short toByte() {
        switch(this) {
        case NORTH:         return 0;
        case NORTH_EAST:    return 1;
        case EAST:          return 2;
        case SOUTH_EAST:    return 3;
        case SOUTH:         return 4;
        case SOUTH_WEST:    return 5;
        case WEST:          return 6;
        case NORTH_WEST:    return 7;
        default:            return 0;
        }
    }

    // angle must be in [0, 360]. 0 is north west and positive angles turn clockwise
    public static Direction fromAngle(double angle) {
        double r = 360 / 16.0;
        if(angle < 45 - r) {
            return Direction.NORTH_WEST;
        } else if(angle < 90 - r) {
            return Direction.NORTH;
        } else if(angle < 135 - r) {
            return Direction.NORTH_EAST;
        } else if(angle < 180 - r) {
            return Direction.EAST;
        } else if(angle < 225 - r) {
            return Direction.SOUTH_EAST;
        } else if(angle < 270 - r) {
            return Direction.SOUTH;
        } else if(angle < 315 - r) {
            return Direction.SOUTH_WEST;
        } else if(angle < 360 - r) {
            return Direction.WEST;
        } else {
            return Direction.NORTH_WEST;
        }
    }

    public int getFrameIndex() {
        switch(this) {
        case NORTH:         return 3;
        case NORTH_EAST:    return 2;
        case EAST:          return 1;
        case SOUTH_EAST:    return 0;
        case SOUTH:         return 1;
        case SOUTH_WEST:    return 2;
        case WEST:          return 3;
        case NORTH_WEST:    return 4;
        default:            return -1;
        }
    }

    public Direction getOpposingDirection() {
        switch(this) {
        case NORTH:         return SOUTH;
        case NORTH_EAST:    return SOUTH_WEST;
        case EAST:          return WEST;
        case SOUTH_EAST:    return NORTH_WEST;
        case SOUTH:         return NORTH;
        case SOUTH_WEST:    return NORTH_EAST;
        case WEST:          return EAST;
        case NORTH_WEST:    return SOUTH_EAST;
        default:            return SOUTH;
        }
    }
}
