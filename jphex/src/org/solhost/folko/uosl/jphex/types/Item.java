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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.common.RandUtil;
import org.solhost.folko.uosl.jphex.engines.Timer;
import org.solhost.folko.uosl.jphex.scripting.ItemBehavior;
import org.solhost.folko.uosl.jphex.scripting.ScriptManager;
import org.solhost.folko.uosl.jphex.world.ObjectRegistry;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.types.Gumps;
import org.solhost.folko.uosl.libuosl.types.Items;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;
import org.solhost.folko.uosl.libuosl.types.Spell;

public class Item extends SLObject implements SendableItem {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger("jphex.types");
    private transient Player draggedBy;
    private transient List<Item> children;
    private transient List<Player> peekingPlayers; // if container so we can send changes to them
    private boolean isContainer, isWearable, isStackable;
    private boolean locked;
    private short weight, defaultLayer;
    private byte lightLevel;
    private int amount, price, height;
    private String behavior;
    private long decayAt;

    {
        this.children = new CopyOnWriteArrayList<Item>();
        this.amount = 1;
    }

    public Item(long serial, int graphic) {
        super(serial);
        this.graphic = graphic;
        setBasicAttributes();
    }

    public Item(long serial, int graphic, String behavior) {
        super(serial);
        this.graphic = graphic; // might be overridden by onCreate
        this.behavior = behavior;
        ItemBehavior ib = ScriptManager.instance().getItemBehavior(behavior);
        if(ib == null) {
            throw new UnsupportedOperationException("invalid behavior");
        } else {
            try {
                ib.onCreate(this);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onCreate: " + e.getMessage(), e);
                delete();
            }
        }
        setBasicAttributes();
    }

    @Override
    public void setGraphic(int graphic) {
        super.setGraphic(graphic);
        setBasicAttributes();
    }

    protected final void setBasicAttributes() {
        StaticTile tile = SLData.get().getTiles().getStaticTile(graphic);
        if(tile != null) {
            this.name = tile.name;
            this.weight = tile.weight;
            this.price = tile.price;
            this.height = tile.height;
            this.defaultLayer = tile.layer;
            this.isContainer = tile.isContainer();
            this.isWearable = tile.isWearable();
            this.isStackable = tile.isStackable();
        }
    }

    @Override
    public void delete() {
        if(children != null) {
            for(Item child : children) {
                child.delete();
            }
            children.clear();
        }
        super.delete();
    }

    @Override
    public void onLoad() {
        ItemBehavior ib = ScriptManager.instance().getItemBehavior(behavior);
        if(ib != null) {
            try {
                ib.onLoad(this);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onLoad: " + e.getMessage(), e);
            }
        }
    }

    public void addPeekingPlayer(Player player) {
        if(peekingPlayers == null) {
            peekingPlayers = new LinkedList<Player>();
        }
        peekingPlayers.add(player);
    }

    public boolean isPlayerPeeking(Player player) {
        if(peekingPlayers == null) {
            return false;
        }
        return peekingPlayers.contains(player);
    }

    public int getHeight() {
        return height;
    }

    public void decayInMillis(long millis) {
        this.decayAt = Timer.getCurrentTicks() + millis;
    }

    public boolean shouldDecay() {
        return !locked && this.decayAt > 0 && Timer.getCurrentTicks() >= this.decayAt;
    }

    public void stopDecay() {
        this.decayAt = 0;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
        ItemBehavior ib = ScriptManager.instance().getItemBehavior(behavior);
        if(ib != null) {
            try {
                ib.onBehaviorChange(this);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Script error in onBehaviorSet: " + e.getMessage(), e);
            }
        }
    }

    public String getBehavior() {
        return behavior;
    }

    public Item createCopy(long serial) {
        Item res = new Item(serial, graphic);
        res.weight = weight;
        res.amount = amount;
        res.hue = hue;
        res.location = new Point3D(location.getX(), location.getY(), location.getZ());
        res.name = new String(name);
        if(behavior != null) {
            res.behavior = new String(behavior);
            for(String prop : scriptProperties.keySet()) {
                res.setProperty(prop, getProperty(prop));
            }
        }
        return res;
    }

    public Item getItemAtLocation(Point2D location) {
        for(Item item : children) {
            if(item.getLocation().equals2D(location)) {
                return item;
            }
        }
        return null;
    }

    public Point3D getRandomContainerLocation() {
        Rectangle rect = Gumps.getGumpDimensions(Gumps.getItemGump(graphic));
        int x = RandUtil.random(rect.x, rect.x + rect.width);
        int y = RandUtil.random(rect.y, rect.y + rect.height);

        return new Point3D(x, y, 0);
    }

    public void addChild(Item child, Point2D location) {
        child.setParent(this);
        if(location.getX() == 0 && location.getY() == 0) {
            location = getRandomContainerLocation();
        }
        if(this.graphic == Items.GFX_SPELLBOOK) {
            child.setAmount(Spell.fromScrollGraphic(child.getGraphic()).toByte());
        } else if(this.graphic == Items.GFX_SHOP_CONTAINER) {
            // the client sorts the shop list by location and uses that as index
            int index = children.size();
            location = new Point2D(index, index);
        }

        child.location = new Point3D(location, 0);
        children.add(child);
        for(ObjectObserver o : observers) o.onChildAdded(this, child);
    }

    // adding or removing has no effect
    public List<Item> getChildren() {
        List<Item> res = new ArrayList<Item>();
        for(Item child : children) {
            res.add(child);
        }
        return res;
    }

    public void removeChild(Item child) {
        children.remove(child);
        for(ObjectObserver o : observers) o.onChildRemoved(this, child);
    }

    public boolean isOnGround() {
        return getParent() == null && location != null;
    }

    public boolean isWorn() {
        return getParent() instanceof Mobile;
    }

    public boolean isInContainer() {
        return getParent() instanceof Item;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && (draggedBy == null);
    }

    public int getAmount() {
        return amount;
    }

    public int getPrice() {
        return price;
    }

    public byte getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(byte level) {
        this.lightLevel = level;
        for(ObjectObserver o : observers) o.onObjectUpdate(this);
    }

    public void consume(int count) {
        if(amount > count) {
            setAmount(amount - count);
        } else if(amount == count) {
            delete();
        } else {
            log.warning(String.format("Consuming %d of %d available for item %08X", count, amount, serial));
        }
    }

    public Item findChildByType(int graphicID) {
        for(Item child : getChildren()) {
            if(child.getGraphic() == graphicID) {
                return child;
            }
        }
        return null;
    }

    public int getAmountByType(int graphicID) {
        int total = 0;

        for(Item child : getChildren()) {
            if(child.getGraphic() == graphicID) {
                total += child.getAmount();
            }
            if(child.isContainer()) {
                total += child.getAmountByType(graphicID);
            }
        }
        return total;
    }

    public void consumeByType(int graphicID, int count) {
        if(getAmountByType(graphicID) < count) {
            log.warning(String.format("Consuming %d of %d available (type %04X) for item %08X", count, getAmountByType(graphicID), graphicID, serial));
            return;
        }

        for(Item child : getChildren()) {
            if(child.getGraphic() == graphicID) {
                if(count >= child.getAmount()) {
                    // Less than we need, take everything from here and go on
                    count -= child.getAmount();
                    child.consume(child.getAmount());
                } else {
                    // More or exactly what we need, take what we need and stop
                    child.consume(count);
                    count = 0;
                    break;
                }
            }
            if(child.isContainer()) {
                int av = child.getAmountByType(graphicID);
                if(count >= av) {
                    child.consumeByType(graphicID, av);
                    count -= av;
                } else {
                    child.consumeByType(graphicID, count);
                    count = 0;
                    break;
                }
            }
        }
        if(count != 0) {
            log.severe("Logic error in consumeByType: " + count + " left of ID " + graphicID);
        }
    }

    public void setDragged(Player who) {
        this.draggedBy = who;
        for(ObjectObserver o : observers) o.onItemDragged(this, who);
    }

    public Player getDraggingPlayer() {
        return draggedBy;
    }

    public void dropped() {
        this.draggedBy = null;
        // dropping will cause setLocation to happen, so no observer here
    }

    public void setAmount(int amount) {
        this.amount = amount;
        for(ObjectObserver o : observers) o.onObjectUpdate(this);
    }

    public boolean isContainer() {
        return isContainer;
    }

    public boolean isWearable() {
        return isWearable;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public short getWeight() {
        return weight;
    }

    public void setWeight(short weight) {
        this.weight = weight;
    }

    public short getLayer() {
        if(graphic == Items.GFX_SHOP_CONTAINER){
            return 9;
        }
        return defaultLayer;
    }

    public static Item createAtLocation(Point3D location, int graphic, int amount) {
        long serial = ObjectRegistry.get().registerItemSerial();

        Item item = new Item(serial, graphic);
        item.setLocation(location);
        item.setAmount(amount);
        ObjectRegistry.get().registerObject(item);

        return item;
    }

    public static Item createEquipped(Mobile on, int graphic, int hue) {
        long serial = ObjectRegistry.get().registerItemSerial();

        Item item = new Item(serial, graphic);
        item.setParent(on);
        item.setHue(hue);
        item.setLocation(new Point3D(0, 0, 0));
        ObjectRegistry.get().registerObject(item);
        on.equipItem(item);

        return item;
    }

    public static Item createInContainer(Item container, int graphic, int amount) {
        long serial = ObjectRegistry.get().registerItemSerial();
        if(container == null) {
            log.severe("tried to create item in null-container");
            return null;
        }

        Item item = new Item(serial, graphic);
        item.setLocation(container.getRandomContainerLocation());
        item.setParent(container);
        item.setAmount(amount);
        ObjectRegistry.get().registerObject(item);
        container.addChild(item, item.getLocation());

        return item;
    }

    public boolean acceptsChild(Item item) {
        if(graphic == Items.GFX_SPELLBOOK) {
            Spell spell = Spell.fromScrollGraphic(item.getGraphic());
            if(spell == null) {
                // trying to insert something that's not a spell into spellbook
                return false;
            }
            for(Item scroll : children) {
                if(scroll.getGraphic() == item.getGraphic()) {
                    // scroll already present
                    return false;
                }
            }
        }

        return true;
    }

    public void onUse(Player player) {
        ItemBehavior ib = ScriptManager.instance().getItemBehavior(behavior);
        if(ib != null) {
            try {
                ib.onUse(player, this);
            } catch(Exception e) {
                log.log(Level.SEVERE, "Exception in onUse: " + e.getMessage(), e);
            }
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.children = new CopyOnWriteArrayList<Item>();
    }

    public int getWeaponDamage() {
        switch(graphic) {
        case Items.GFX_GREAT_AXE:           return RandUtil.random(16, 20);
        case Items.GFX_EXECUTIONER_AXE:     return RandUtil.random(15, 19);
        case Items.GFX_HAND_AXE:            return RandUtil.random(13, 17);
        case Items.GFX_BATTLE_AXE:          return RandUtil.random(17, 20);
        case Items.GFX_WAR_AXE:             return RandUtil.random(14, 18);
        case Items.GFX_DAGGER:              return RandUtil.random(10, 13);
        case Items.GFX_MACE:                return RandUtil.random(11, 16);
        case Items.GFX_SHORT_SWORD:         return RandUtil.random(11, 16);
        case Items.GFX_BROAD_SWORD:         return RandUtil.random(13, 18);
        default: log.warning("Unknown base damage for graphic " + graphic);
            return RandUtil.random(1, 10);
        }
    }

    // in ticks, i.e. units of 250ms
    public int getWeaponSpeed() {
        switch(graphic) {
        case Items.GFX_GREAT_AXE:           return 14;
        case Items.GFX_EXECUTIONER_AXE:     return 13;
        case Items.GFX_HAND_AXE:            return 11;
        case Items.GFX_BATTLE_AXE:          return 12;
        case Items.GFX_WAR_AXE:             return 13;
        case Items.GFX_DAGGER:              return 8;
        case Items.GFX_MACE:                return 11;
        case Items.GFX_SHORT_SWORD:         return 11;
        case Items.GFX_BROAD_SWORD:         return 13;
        default: log.warning("Unknown weapon speed for graphic " + graphic);
            return 12;
        }
    }

    public double getArmorRating() {
        switch(graphic) {
            case Items.GFX_HEATER:      return 0.2;
            case Items.GFX_BRACERS:     return 0.05;
            case Items.GFX_VAMBRACES:   return 0.1;
            case Items.GFX_PANTS:       return 0.01;
            case Items.GFX_LEGGINGS:    return 0.05;
            case Items.GFX_GREAVES:     return 0.15;
            case Items.GFX_TUNIC:       return 0.01;
            case Items.GFX_JERKIN:      return 0.05;
            case Items.GFX_BREASTPLATE: return 0.15;
            case Items.GFX_LEATHER_CAP: return 0.05;
            case Items.GFX_HELMET:      return 0.1;
            case Items.GFX_GORGET:      return 0.1;
            default:
                log.warning("Unknown armor rating for graphic " + graphic);
                return 0;
        }
    }

    @Override
    public short getFacingOverride() {
        if(graphic == Items.GFX_DARKSOURCE) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void foundOrphan(SLObject orphan) {
        addChild((Item) orphan, orphan.getLocation());
    }

    @Override
    public int getLookingHeight() {
        return height;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }
}
