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

import java.util.Collection;

import org.jruby.RubyProc;
import org.jruby.runtime.builtin.IRubyObject;
import org.solhost.folko.uosl.jphex.types.Item;
import org.solhost.folko.uosl.jphex.types.Mobile;
import org.solhost.folko.uosl.jphex.types.NPC;
import org.solhost.folko.uosl.jphex.types.Player;
import org.solhost.folko.uosl.jphex.types.SLObject;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.types.Attribute;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.libuosl.types.Spell;

public interface ScriptAPI {
    public SLObject findObject(long serial);

    public void targetObject(Player player, RubyProc block);
    public void targetLocation(Player player, RubyProc block);

    public void kill(Mobile what);

    public int getDistance(SLObject o1, SLObject o2);
    public boolean canSee(SLObject o1, SLObject o2);
    public boolean canSee(SLObject o1, Point3D location);
    public Point3D getRandomPointInRange(Point3D src, int range);

    public void sendSysMessage(Mobile mob, String message);

    public void setName(SLObject obj, String name);
    public void setGraphic(SLObject obj, int graphic);
    public void setHue(SLObject obj, int hue);
    public void setAttribute(Mobile mob, Attribute attribute, long value);

    public void deleteObject(SLObject obj);
    public void moveObject(SLObject obj, int x, int y);
    public void moveObject(SLObject obj, int x, int y, int z);

    public void sendHexPacket(Player player, short packetID, String data);

    public boolean reloadScripts();
    public void saveWorld();

    public long getTimerTicks();
    public void addTimer(long delayUntilRunInMillis, RubyProc block);

    public Item createItemInBackpack(Mobile mob, int graphic);
    public Item createItemAtMobile(Mobile mob, int graphic);
    public Item createItemAtLocation(int x, int y, int z, int graphic);
    public Item createItemInBackpack(Mobile mob, int graphic, String behavior);
    public Item createItemAtMobile(Mobile mob, int graphic, String behavior);
    public Item createItemAtLocation(int x, int y, int z, int graphic, String behavior);
    public Item createItemInContainer(Item container, int graphic);
    public Item createItemInContainer(Item container, int graphic, String behavior);
    public boolean setItemBehavior(Item item, String behavior);
    public String getItemBehavior(Item item);

    public void setObjectProperty(SLObject obj, String name, IRubyObject value);
    public IRubyObject getObjectProperty(SLObject obj, String name);

    public void playSoundNearObj(SLObject obj, int soundID);
    public void sendSound(Player player, int soundID);

    public Collection<Player> getNearbyPlayers(Mobile who);
    public Collection<Mobile> getMobilesInRange(SLObject obj, int range);
    public NPC spawnMobileAtPlayer(Player near, String behavior);
    public NPC spawnMobileAtLocation(int x, int y, int z, String behavior);
    public void assignRandomName(Mobile mob, String suffix);
    public void createClothes(Mobile mob);
    public void say(SLObject mob, String text);
    public void offerShop(Mobile mob, Player player);
    public void refreshStats(Mobile mob);
    public void lookAt(Mobile who, SLObject what);
    public boolean runToward(Mobile who, Mobile to);
    public boolean runAway(Mobile who, Mobile from);
    public void attack(Mobile attacker, Mobile defender);
    public Mobile getNearestMobile(Mobile from);

    public int randomHairStyle(int graphic);
    public int randomHairHue();
    public int randomClothColor();

    public Collection<SLStatic> getStaticsAtLocation(int x, int y);
    public Collection<Item> getItemsAtLocation(int x, int y);
    public Collection<Item> getItemsAtLocation(int x, int y, int z);

    public void speakPowerWords(Player player, Spell spell);
    public void throwFireball(Player player, Mobile target);

    public boolean checkSkill(Mobile mob, Attribute toCheck, long minRequired, long maxUntilNoGain);
}
