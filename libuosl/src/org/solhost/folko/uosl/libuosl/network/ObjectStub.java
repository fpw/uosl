package org.solhost.folko.uosl.libuosl.network;

import org.solhost.folko.uosl.libuosl.types.Point3D;

public class ObjectStub implements SendableObject {
    private long serial;
    private String name;
    private int graphic, hue;
    private Point3D location;

    public void setSerial(long serial) {
        this.serial = serial;
    }

    @Override
    public long getSerial() {
        return serial;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setGraphic(int graphic) {
        this.graphic = graphic;
    }

    @Override
    public int getGraphic() {
        return graphic;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    @Override
    public int getHue() {
        return hue;
    }

    public void setLocation(Point3D location) {
        this.location = location;
    }

    @Override
    public Point3D getLocation() {
        return location;
    }
}
