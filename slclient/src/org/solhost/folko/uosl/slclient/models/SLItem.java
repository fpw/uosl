package org.solhost.folko.uosl.slclient.models;

import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.network.SendableItem;

public class SLItem extends SLObject implements SendableItem {
    private short layer, facingOverride;
    private int amount;
    private StaticTile tileInfo;
    private boolean isStatic;

    public SLItem(long serial, int graphic) {
        super(serial, graphic);
        isStatic = false;
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

    public boolean isStatic() {
        return isStatic;
    }
}
