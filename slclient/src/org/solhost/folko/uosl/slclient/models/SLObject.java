package org.solhost.folko.uosl.slclient.models;

import org.solhost.folko.uosl.libuosl.network.SendableObject;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public abstract class SLObject implements SendableObject {
    protected long serial;
    protected Point3D location;
    protected int graphic, hue;
    protected String name;
    protected boolean registered;

    public SLObject(long serial, int graphic) {
        setSerial(serial);
        setGraphic(graphic);
        registered = false;
    }

    public void register() {
        registered = true;
    }

    public void unregister() {
        registered = false;
    }

    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public Point3D getLocation() {
        return location;
    }

    public void setLocation(Point3D location) {
        this.location = location;
    }

    public int getGraphic() {
        return graphic;
    }

    public void setGraphic(int graphic) {
        this.graphic = graphic;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
