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
package org.solhost.folko.uosl.jphex.scripting;

import org.solhost.folko.uosl.jphex.types.Item;
import org.solhost.folko.uosl.jphex.types.Mobile;
import org.solhost.folko.uosl.jphex.types.Player;

public interface MobileBehavior {
    public void onSpawn(Mobile mob);
    public void onLoad(Mobile mob);
    public void onEnterArea(Mobile mob, Player player);
    public void onHello(Mobile mob, Player player); // when a player says "Hello" and we're the nearest NPC
    public boolean onDoubleClick(Mobile mob, Player player); // return true if paperdoll should be opened
    public void onSpeech(Mobile mob, Player player, String line);
    public void onAttacked(Mobile mob, Mobile attacker);
    public void onDeath(Mobile mob, Item corpse);
}
