package org.solhost.folko.uosl.slclient.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.solhost.folko.uosl.libuosl.network.SendableObject;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public abstract class SLObject implements SendableObject {
    protected LongProperty serial;
    protected Property<Point3D> location;
    protected IntegerProperty graphic, hue;
    protected StringProperty name;

    public SLObject(long serial, int graphic) {
        this.serial = new SimpleLongProperty();
        this.location = new SimpleObjectProperty<Point3D>();
        this.graphic = new SimpleIntegerProperty();
        this.hue = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();

        setSerial(serial);
        setGraphic(graphic);
    }

    public long getSerial() {
        return serial.get();
    }

    public ReadOnlyLongProperty serialProperty() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial.set(serial);
    }

    public Point3D getLocation() {
        return location.getValue();
    }

    public ReadOnlyProperty<Point3D> locationProperty() {
        return location;
    }

    public void setLocation(Point3D location) {
        this.location.setValue(location);
    }

    public int getGraphic() {
        return graphic.get();
    }

    public ReadOnlyIntegerProperty graphicProperty() {
        return graphic;
    }

    public void setGraphic(int graphic) {
        this.graphic.set(graphic);
    }

    public int getHue() {
        return hue.get();
    }

    public ReadOnlyIntegerProperty hueProperty() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue.set(hue);
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }
}
