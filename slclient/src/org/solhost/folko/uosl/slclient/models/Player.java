package org.solhost.folko.uosl.slclient.models;

public class Player extends SLMobile {
    private boolean warMode;

    public Player(long serial, int graphic) {
        super(serial, graphic);
    }

    public boolean isInWarMode() {
        return warMode;
    }
}
