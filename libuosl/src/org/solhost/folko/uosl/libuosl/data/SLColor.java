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

public class SLColor {
    public static int convert555(int color, int alpha) {
        // 54321098 76543210
        // 11111100 00000000
        // rrrrrggg gggbbbbb
        int red, green, blue;

        if(color >>> 16 != 0) throw new RuntimeException("not a 555 color");

        blue =  (color & 0x1F) * 255 / 31;
        color >>>= 5;
        green = (color & 0x1F) * 255 / 31;
        color >>>= 5;
        red =   (color & 0x1F) * 255 / 31;
        color >>>= 5;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
