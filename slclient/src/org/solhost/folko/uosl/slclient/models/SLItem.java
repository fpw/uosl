package org.solhost.folko.uosl.slclient.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.network.SendableItem;

public class SLItem extends SLObject implements SendableItem {
    private short layer, facingOverride;
    private int amount;
    private StaticTile tileInfo;
    private boolean isStatic;
    private SLObject parent;
    private final Set<SLItem> containerContents;

    public SLItem(long serial, int graphic) {
        super(serial, graphic);
        isStatic = false;
        containerContents = new HashSet<SLItem>();
    }

    public static SLItem fromStatic(SLStatic stat) {
        SLItem res = new SLItem(stat.getSerial(), stat.getStaticID());
        res.setLocation(stat.getLocation());
        res.setHue(stat.getHue());
        res.setLayer(res.tileInfo.layer);
        res.setName(res.tileInfo.name);
        res.setFacingOverride((short) 0);
        res.setAmount(1);
        res.isStatic = true;
        return res;
    }

    @Override
    public void setGraphic(int graphic) {
        super.setGraphic(graphic);
        tileInfo = SLData.get().getTiles().getStaticTile(graphic);
        if(tileInfo != null && tileInfo.name != null) {
            setName(tileInfo.name);
        }
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    public void setLayer(short layer) {
        this.layer = layer;
    }

    @Override
    public short getLayer() {
        return layer;
    }

    public void setFacingOverride(short override) {
        this.facingOverride = override;
    }

    @Override
    public short getFacingOverride() {
        return facingOverride;
    }

    public StaticTile getTileInfo() {
        return tileInfo;
    }

    public void setWorn(SLMobile byWhom) {
        this.parent = byWhom;
    }

    @Override
    public void unregister() {
        if(parent instanceof SLItem) {
            ((SLItem) parent).removeFromContainer(this);
        } else if(parent instanceof SLMobile) {
            ((SLMobile) parent).unequip(this);
        }
        super.unregister();
    }

    public void setContainer(SLItem container) {
        this.parent = container;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isOnGround() {
        return parent == null;
    }

    public boolean isWorn() {
        return parent instanceof SLMobile;
    }

    public boolean isInContainer() {
        return parent instanceof SLItem;
    }

    public void addToContainer(SLItem itm) {
        containerContents.add(itm);
        itm.setContainer(this);
    }

    public void removeFromContainer(SLItem itm) {
        containerContents.remove(itm);
        itm.setContainer(null);
    }

    public Set<SLItem> getContainerContents() {
        return Collections.unmodifiableSet(containerContents);
    }
}
