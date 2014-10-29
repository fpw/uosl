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
package org.solhost.folko.uosl.common;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandUtil {
    // [minimum, maximum)
    public static int random(int min, int max) {
        Random rng = ThreadLocalRandom.current();
        return rng.nextInt(max - min) + min;
    }

    public static <T extends Object> T randomElement(T[] array) {
        if(array.length == 0) return null;
        int index = random(0, array.length);
        return array[index];
    }

    public static boolean tryChance(double chance) {
        Random rng = ThreadLocalRandom.current();
        return rng.nextDouble() <= chance;
    }
}
