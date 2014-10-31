package org.solhost.folko.uosl.slclient.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.data.SLStatic;
import org.solhost.folko.uosl.libuosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.libuosl.network.SendableItem;

public class SLItem extends SLObject implements SendableItem {
    private final IntegerProperty amount;
    private final Property<Short> layer, facingOverride;
    private StaticTile tileInfo;
    private boolean isStatic;

    public SLItem(long serial, int graphic) {
        super(serial, graphic);
        amount = new SimpleIntegerProperty();
        layer = new SimpleObjectProperty<>();
        facingOverride = new SimpleObjectProperty<>();
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
        this.amount.set(amount);
    }

    public ReadOnlyIntegerProperty amountProperty() {
        return amount;
    }

    @Override
    public int getAmount() {
        return amount.get();
    }

    public void setLayer(short layer) {
        this.layer.setValue(layer);
    }

    public ReadOnlyProperty<Short> layerProperty() {
        return layer;
    }

    @Override
    public short getLayer() {
        return layer.getValue();
    }

    public void setFacingOverride(short override) {
        this.facingOverride.setValue(override);
    }

    public ReadOnlyProperty<Short> facingOverrideProperty() {
        return facingOverride;
    }

    @Override
    public short getFacingOverride() {
        return facingOverride.getValue();
    }

    public StaticTile getTileInfo() {
        return tileInfo;
    }

    public boolean isStatic() {
        return isStatic;
    }
}
