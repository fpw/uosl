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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.jruby.RubyObject;
import org.jruby.RubyProc;
import org.jruby.runtime.builtin.IRubyObject;
import org.solhost.folko.uosl.common.RandUtil;
import org.solhost.folko.uosl.jphex.engines.Timer;
import org.solhost.folko.uosl.jphex.engines.TimerQueue;
import org.solhost.folko.uosl.jphex.types.*;
import org.solhost.folko.uosl.jphex.world.ObjectRegistry;
import org.solhost.folko.uosl.jphex.world.World;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.network.packets.FightPacket;
import org.solhost.folko.uosl.libuosl.types.Attribute;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Items;
import org.solhost.folko.uosl.libuosl.types.Mobiles;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.libuosl.types.Spell;
import org.solhost.folko.uosl.libuosl.util.Pathfinder;

public class ScriptAPIImpl implements ScriptAPI {
    private static final Logger log = Logger.getLogger("jphex.scriptapi");
    private final World world;
    private final ObjectRegistry registry;

    public ScriptAPIImpl(World world) {
        this.world = world;
        this.registry = ObjectRegistry.get();
    }

    @Override
    public void sendSysMessage(Mobile mob, String message) {
        if(mob instanceof Player) {
            ((Player) mob).sendSysMessage(message);
        }
    }

    @Override
    public void moveObject(SLObject obj, int x, int y) {
        byte z = SLData.get().getMap().getTileElevation(new Point2D(x, y));
        moveObject(obj, x, y, z);
    }

    @Override
    public void moveObject(SLObject obj, int x, int y, int z) {
        if(obj instanceof Item && obj.getParent() != null) {
            SLObject parent = obj.getParent();
            obj.setParent(null);
            if(parent instanceof Item) {
                ((Item) parent).removeChild((Item) obj);
            } else if(parent instanceof Mobile) {
                ((Mobile) parent).unequipItem((Item) obj);
            }
        }
        obj.setLocation(new Point3D(x, y, z));
    }

    @Override
    public void sendHexPacket(Player player, short packetID, String data) {
        player.sendHexPacket(packetID, data);
    }

    @Override
    public boolean reloadScripts() {
        return ScriptManager.instance().reload();
    }

    @Override
    public void saveWorld() {
        world.save();
    }

    @Override
    public void playSoundNearObj(SLObject obj, int soundID) {
        Point2D location = null;
        if(obj instanceof Item) {
            Item itm = (Item) obj;
            if(itm.isOnGround()) {
                location = obj.getLocation();
            } else if(itm.isWorn()) {
                location = obj.getParent().getLocation();
            } else if(itm.isInContainer()) {
                playSoundNearObj(itm, soundID);
                return;
            }
        } else {
            location = obj.getLocation();
        }

        for(Player player : world.getOnlinePlayersInRange(location, World.VISIBLE_RANGE)) {
            player.sendSound(soundID);
        }
    }

    @Override
    public void sendSound(Player player, int soundID) {
        player.sendSound(soundID);
    }

    @Override
    public Item createItemInBackpack(Mobile mob, int graphic) {
        Item backpack = mob.getBackpack();
        if(backpack != null) {
            Item item = new Item(registry.registerItemSerial(), graphic);
            registry.registerObject(item);
            backpack.addChild(item, new Point2D(0, 0));
            return item;
        } else {
            return createItemAtMobile(mob, graphic);
        }
    }

    @Override
    public Item createItemAtMobile(Mobile mob, int graphic) {
        Item item = new Item(registry.registerItemSerial(), graphic);
        item.setLocation(mob.getLocation());
        registry.registerObject(item);
        return item;
    }

    @Override
    public Item createItemAtLocation(int x, int y, int z, int graphic) {
        Item item = new Item(registry.registerItemSerial(), graphic);
        item.setLocation(new Point3D(x, y, z));
        registry.registerObject(item);
        return item;
    }

    @Override
    public Item createItemInBackpack(Mobile mob, int graphic, String behavior) {
        Item backpack = mob.getBackpack();
        if(backpack != null) {
            try {
                Item item = new Item(registry.registerItemSerial(), graphic, behavior);
                registry.registerObject(item);
                backpack.addChild(item, new Point2D(0, 0));
                return item;
            } catch (Exception e) {
                return null;
            }
        } else {
            return createItemAtMobile(mob, graphic, behavior);
        }
    }

    @Override
    public Item createItemAtMobile(Mobile mob, int graphic, String behavior) {
        try {
            Item item = new Item(registry.registerItemSerial(), graphic, behavior);
            item.setLocation(mob.getLocation());
            registry.registerObject(item);
            return item;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public Item createItemAtLocation(int x, int y, int z, int graphic, String behavior) {
        try {
            Item item = new Item(registry.registerItemSerial(), graphic, behavior);
            item.setLocation(new Point3D(x, y, z));
            registry.registerObject(item);
            return item;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public Item createItemInContainer(Item container, int graphic) {
        try {
            Item item = new Item(registry.registerItemSerial(), graphic);
            registry.registerObject(item);
            container.addChild(item, new Point2D(0, 0));
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Item createItemInContainer(Item container, int graphic, String behavior) {
        try {
            Item item = new Item(registry.registerItemSerial(), graphic, behavior);
            registry.registerObject(item);
            container.addChild(item, new Point2D(0, 0));
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setGraphic(SLObject obj, int graphic) {
        obj.setGraphic(graphic);
    }

    @Override
    public void setHue(SLObject obj, int hue) {
        obj.setHue(hue);
    }

    @Override
    public void targetObject(Player player, final RubyProc block) {
        player.targetObject(new TargetObjectHandler() {
            public void onTarget(SLObject obj) {
                IRubyObject[] args = {ScriptManager.instance().toRubyObject(obj)};
                block.call(ScriptManager.instance().getContext(), args);
            }
        });
    }

    @Override
    public void targetLocation(Player player, final RubyProc block) {
        player.targetLocation(new TargetLocationHandler() {
            public void onTarget(Point3D point) {
                IRubyObject[] args = {
                            ScriptManager.instance().toRubyObject(point.getX()),
                            ScriptManager.instance().toRubyObject(point.getY()),
                            ScriptManager.instance().toRubyObject(point.getZ())
                        };
                block.call(ScriptManager.instance().getContext(), args);
            }
        });
    }

    @Override
    public void deleteObject(SLObject obj) {
        obj.delete();
    }

    @Override
    public boolean setItemBehavior(Item item, String behavior) {
        if(ScriptManager.instance().getItemBehavior(behavior) != null) {
            item.setBehavior(behavior);
            return true;
        } else if(behavior.equals("null")) {
            item.setBehavior(null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getItemBehavior(Item item) {
        return item.getBehavior();
    }

    @Override
    public void setObjectProperty(SLObject object, String name, IRubyObject value) {
        if(!(value instanceof RubyObject)) {
            throw new UnsupportedOperationException("can only store real RubyObjects");
        }
        object.setProperty(name, (RubyObject) value);
    }

    @Override
    public IRubyObject getObjectProperty(SLObject object, String name) {
        IRubyObject value = object.getProperty(name);
        if(value != null) {
            return value;
        } else {
            return ScriptManager.instance().toRubyObject(null);
        }
    }

    @Override
    public void addTimer(long millis, final RubyProc block) {
        TimerQueue.get().addTimer(new Timer(millis, new Runnable() {
            public void run() {
                IRubyObject args[] = {};
                block.call(ScriptManager.instance().getContext(), args);
            }
        }));
    }

    @Override
    public NPC spawnMobileAtPlayer(Player near, String behavior) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be == null) {
            return null;
        }
        NPC npc = new NPC(registry.registerMobileSerial());
        npc.setLocation(near.getLocation());
        npc.setFacing(near.getFacing());
        npc.setBehavior(behavior);
        be.onSpawn(npc);
        registry.registerObject(npc);
        world.npcPlayerSearch(npc);
        return npc;
    }

    @Override
    public NPC spawnMobileAtLocation(int x, int y, int z, String behavior) {
        MobileBehavior be = ScriptManager.instance().getMobileBehaviour(behavior);
        if(be == null) {
            return null;
        }
        NPC npc = new NPC(registry.registerMobileSerial());
        npc.setLocation(new Point3D(x, y, z));
        npc.setBehavior(behavior);
        be.onSpawn(npc);
        registry.registerObject(npc);
        world.npcPlayerSearch(npc);
        return npc;
    }

    @Override
    public void setName(SLObject obj, String name) {
        obj.setName(name);
    }

    @Override
    public void assignRandomName(Mobile mob, String suffix) {
        String name;
        switch(mob.getGraphic()) {
        case 0x0000: { // human male
            String[] randomNames = {"Adam", "Bob", "Charles", "David", "Eric", "Frank", "George", "Thomas", "James"};
            name = RandUtil.randomElement(randomNames);
            break;
        }
        case 0x0001: { // human female
            String[] randomNames = {"Arlene", "Charlotte"};
            name = RandUtil.randomElement(randomNames);
            break;
        }
        default:
            name = "unnamed";
            break;
        }
        if(suffix.length() > 0) {
            mob.setName(name + " " + suffix);
        } else {
            mob.setName(name);
        }
    }

    @Override
    public int randomHairStyle(int graphic) {
        if(graphic == 0) {
            return Items.GFX_HAIR_START + RandUtil.random(0, 2);
        } else {
            return Items.GFX_HAIR_START + 3 + RandUtil.random(0, 3);
        }
    }

    @Override
    public int randomHairHue() {
        return RandUtil.random(7, 15);
    }

    @Override
    public int randomClothColor() {
        return RandUtil.random(23, 40);
    }

    @Override
    public void createClothes(Mobile mob) {
        Item.createEquipped(mob, Items.GFX_INVIS_PACK, 0);
        Item.createEquipped(mob, Items.GFX_SHOP_CONTAINER, 0);
        Item.createEquipped(mob, Items.GFX_TUNIC, randomClothColor());
        Item.createEquipped(mob, randomHairStyle(mob.getGraphic()), randomHairHue());
        if(mob.getGraphic() == Mobiles.MOBTYPE_HUMAN_MALE) {
            Item.createEquipped(mob, Items.GFX_PANTS, randomClothColor());
        } else {
            Item.createEquipped(mob, Items.GFX_SKIRT, randomClothColor());
        }
    }

    @Override
    public void say(SLObject obj, String text) {
        world.sayAbove(obj, text);
    }

    @Override
    public void offerShop(Mobile mob, Player player) {
        world.sendShop(player, mob);
    }

    @Override
    public void speakPowerWords(Player player, Spell spell) {
        world.sayAbove(player, Character.toString((char) 0x0F) + " " + spell.getPowerWords() + " " + Character.toString((char) 0x0F), 0x00887766);
    }

    @Override
    public void setAttribute(Mobile mob, Attribute attr, long value) {
        mob.setAttribute(attr, value);
    }

    @Override
    public void refreshStats(Mobile mob) {
        mob.refreshStats();
    }

    @Override
    public int getDistance(SLObject o1, SLObject o2) {
        return o1.distanceTo(o2);
    }

    @Override
    public boolean runToward(Mobile who, Mobile to) {
        int distance = who.distanceTo(to);
        if(distance <= 1) {
            // already there
            return true;
        } else if(distance > World.VISIBLE_RANGE) {
            // too far away
            return false;
        } else {
            // try running next step
            Pathfinder finder = new Pathfinder(who.getLocation(), to.getLocation(), world);
            if(!finder.findPath(500)) {
                // couldn't find a path
                return false;
            }
            Direction dir = finder.getPath().get(0);
            who.setFacing(dir);
            Point3D newLoc = world.canWalk(who, dir);
            if(newLoc == null) {
                log.severe("Pathfinder returned illegal path");
                return false;
            }
            who.setLocation(newLoc);
            // found a path and walked toward
            return true;
        }
    }

    @Override
    public boolean runAway(Mobile who, Mobile from) {
        int distance = who.distanceTo(from);
        if(distance > World.VISIBLE_RANGE) {
            // already away
            return false;
        } else {
            // try running next step
            Direction dir = who.getLocation().getDirectionTo(from.getLocation()).getOpposingDirection();
            who.setFacing(dir);
            Point3D newLoc = world.canWalk(who, dir);
            if(newLoc == null) {
                return false;
            }
            who.setLocation(newLoc);
            return true;
        }
    }

    @Override
    public void attack(Mobile attacker, Mobile defender) {
        attacker.setOpponent(defender);
    }

    @Override
    public void kill(Mobile what) {
        what.kill();
    }

    @Override
    public void lookAt(Mobile who, SLObject what) {
        who.setFacing(who.getLocation().getDirectionTo(what.getLocation()));
    }

    @Override
    public Collection<Player> getNearbyPlayers(Mobile who) {
        Collection<Player> online = world.getOnlinePlayersInRange(who.getLocation(), World.VISIBLE_RANGE);
        Iterator<Player> iter = online.iterator();
        while(iter.hasNext()) {
            Player p = iter.next();
            if(!p.isVisible()) {
                iter.remove();
            }
        }
        return online;
    }

    @Override
    public long getTimerTicks() {
        return Timer.getCurrentTicks();
    }

    @Override
    public Collection<SLStatic> getStaticsAtLocation(int x, int y) {
        return SLData.get().getStatics().getStatics(new Point2D(x, y));
    }

    @Override
    public Collection<Item> getItemsAtLocation(int x, int y, int z) {
        List<Item> res = new LinkedList<Item>();
        for(SLObject obj : world.getObjectsInRange(new Point2D(x, y), 0)) {
            if(obj instanceof Item && obj.getLocation().getZ() == z) {
                res.add((Item) obj);
            }
        }
        return res;
    }

    @Override
    public Collection<Item> getItemsAtLocation(int x, int y) {
        List<Item> res = new LinkedList<Item>();
        for(SLObject obj : world.getObjectsInRange(new Point2D(x, y), 0)) {
            if(obj instanceof Item) {
                res.add((Item) obj);
            }
        }
        return res;
    }

    @Override
    public SLObject findObject(long serial) {
        return registry.findObject(serial);
    }

    @Override
    public boolean checkSkill(Mobile mob, Attribute toCheck, long minRequired, long maxUntilNoGain) {
        return mob.checkSkill(toCheck, minRequired, maxUntilNoGain);
    }

    @Override
    public void throwFireball(Player player, Mobile target) {
        player.lookAt(target);
        FightPacket packet = new FightPacket(true, player, target);
        for(Player p : world.getInterestedPlayers(target)) {
            p.sendPacket(packet);
        }
    }

    @Override
    public Collection<Mobile> getMobilesInRange(SLObject rangeObj, int range) {
        List<Mobile> res = new LinkedList<Mobile>();
        for(SLObject obj : world.getObjectsInRange(rangeObj.getLocation(), range)) {
            if(obj == rangeObj) continue;
            if(obj instanceof Mobile && obj.isVisible()) {
                res.add((Mobile) obj);
            }
        }
        return res;
    }

    @Override
    public Mobile getNearestMobile(Mobile from) {
        int nearest = Integer.MAX_VALUE;
        Mobile res = null;
        for(Mobile mob : getMobilesInRange(from, World.VISIBLE_RANGE)) {
            if(mob == from) continue;
            int dist = from.distanceTo(mob);
            if(dist < nearest) {
                nearest = dist;
                res = mob;
            }
        }
        return res;
    }

    @Override
    public boolean canSee(SLObject o1, SLObject o2) {
        return o1.canSee(o2, World.VISIBLE_RANGE, world);
    }

    @Override
    public boolean canSee(SLObject o1, Point3D location) {
        return o1.canSee(location, World.VISIBLE_RANGE, world);
    }

    @Override
    public Point3D getRandomPointInRange(Point3D src, int range) {
        if(range == 0) {
            return src;
        }

        Point3D cur = src, last = src;
        for(int i = 0; i < range; i++) {
            Direction dir = RandUtil.randomElement(Direction.values());
            cur = SLData.get().getElevatedPoint(cur, dir, world);
            if(cur == null) {
                break;
            } else {
                last = cur;
            }
        }
        return last;
    }
}
