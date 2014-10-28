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

public class Mobiles {
    // Hardcoded into client: serials above are considered items
    public static final long SERIAL_FIRST           = 0x00000001;
    public static final long SERIAL_LAST            = 0x3FFFFFFF;

    public static final int MOBTYPE_HUMAN_MALE      = 0x00;
    public static final int MOBTYPE_HUMAN_FEMALE    = 0x01;
    public static final int MOBTYPE_PALE_HUMAN      = 0x03;

    public static final int MOBTYPE_GUARD           = 0x2E;
    public static final int MOBTYPE_LORD_BRITISH    = 0x2F;

    public static final int MOBTYPE_ORC             = 0x28;
    public static final int MOBTYPE_ORC_CAPTAIN     = 0x29;
    public static final int MOBTYPE_SKELETON        = 0x2A;
    public static final int MOBTYPE_WOLF            = 0x32;
    public static final int MOBTYPE_DEER            = 0x34;
    public static final int MOBTYPE_RABBIT          = 0x35;
}
