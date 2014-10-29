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
package org.solhost.folko.uosl.jphex.types;

import org.solhost.folko.uosl.libuosl.types.Attribute;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public interface ObjectObserver {
    public void onObjectUpdate(SLObject src);
    public void onObjectDelete(SLObject src);
    public void onLocationChanged(SLObject src, Point3D oldLoc);
    public void onItemDragged(Item src, Player who);
    public void onAttributeChanged(Mobile src, Attribute a);
    public void onItemEquipped(Item item, Mobile mob);
    public void onChildAdded(Item container, Item child);
    public void onChildRemoved(Item container, Item child);
    public void onDeath(Mobile mob);
    public void onOpponentChanged(Mobile mob, Mobile victim, Mobile oldVictim);
}
