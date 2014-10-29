package org.solhost.folko.uosl.slclient.models;

public class Player extends SLMobile {
    private String password;
    private boolean warMode;

    public Player(long serial, int graphic) {
        super(serial, graphic);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public boolean isInWarMode() {
        return warMode;
    }
}
