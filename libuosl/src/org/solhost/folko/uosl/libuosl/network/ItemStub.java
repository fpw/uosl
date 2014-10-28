package org.solhost.folko.uosl.libuosl.network;


public class ItemStub extends ObjectStub implements SendableItem {
    private int amount;
    private short layer, facingOverride;

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

    public void setFacingOverride(short facingOverride) {
        this.facingOverride = facingOverride;
    }

    @Override
    public short getFacingOverride() {
        return facingOverride;
    }

}
