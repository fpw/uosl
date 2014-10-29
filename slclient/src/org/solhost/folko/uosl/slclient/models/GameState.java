package org.solhost.folko.uosl.slclient.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.lwjgl.Sys;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.network.SendableObject;
import org.solhost.folko.uosl.libuosl.network.packets.LoginPacket;
import org.solhost.folko.uosl.libuosl.network.packets.MoveRequestPacket;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Items;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class GameState {
    public enum State {DISCONNECTED, CONNECTED, LOGGED_IN};

    private static final Logger log = Logger.getLogger("slclient.game");
    private final Player player;
    private final Property<State> state;
    private final ObservableMap<Long, SLObject> objectsInRange;
    private Connection connection;
    private int updateRange = 15;

    // Movement
    private final int MOVE_DELAY = 150;
    private short nextMoveSequence, lastAckedMoveSequence;
    private long lastMoveTime;

    public GameState() {
        state = new SimpleObjectProperty<GameState.State>(State.DISCONNECTED);
        player = new Player(-1, -1);
        objectsInRange = FXCollections.observableMap(new HashMap<Long, SLObject>());

        player.locationProperty().addListener((Player, oldLoc, newLoc) -> onPlayerLocationChange(oldLoc, newLoc));

        log.fine("Game state initialized");
    }

    public ObservableMap<Long, SLObject> objectsInRangeProperty() {
        return objectsInRange;
    }

    public ReadOnlyProperty<State> stateProperty() {
        return state;
    }

    public synchronized State getState() {
        return state.getValue();
    }

    public synchronized Player getPlayer() {
        return player;
    }

    public synchronized void onConnect(Connection connection) {
        this.connection = connection;
        state.setValue(State.CONNECTED);
    }

    public synchronized void onDisconnect() {
        this.connection = null;
        state.setValue(State.DISCONNECTED);
    }

    public synchronized void tryLogin() {
        LoginPacket login = new LoginPacket();
        login.setName(player.getName());
        login.setPassword(player.getPassword());
        login.setSeed(LoginPacket.LOGIN_BY_NAME);
        login.setSerial(LoginPacket.LOGIN_BY_NAME);
        login.prepareSend();
        connection.sendPacket(login);
    }

    public synchronized void onLoginSuccess() {
        state.setValue(State.LOGGED_IN);
    }

    public synchronized int getUpdateRange() {
        return updateRange;
    }

    public synchronized void setUpdateRange(int updateRange) {
        this.updateRange = updateRange;
        onPlayerLocationChange(player.getLocation(), player.getLocation());
    }

    public synchronized void forEachObjectAt(Point2D point, Consumer<SLObject> c) {
        for(SLObject obj : objectsInRange.values()) {
            if(point.equals(obj.getLocation())) {
                c.accept(obj);
            }
        }
    }

    public synchronized void playerMoveRequest(Direction dir) {
        if(lastMoveTime + MOVE_DELAY > getTimeMillis()) {
            // too fast
            return;
        }

        if(nextMoveSequence - lastAckedMoveSequence > 3) {
            log.finer("Disallowing move due to missing acks, want seq: " + nextMoveSequence + ", last ack: " + lastAckedMoveSequence);
            return;
        }

        if(player.getFacing() != dir) {
            // only turning
            MoveRequestPacket packet = new MoveRequestPacket(dir, nextMoveSequence++, false);
            connection.sendPacket(packet);
            player.setFacing(dir);
            lastMoveTime = getTimeMillis();
        } else {
            Point3D oldLoc = player.getLocation();
            Point3D newLoc = SLData.get().getElevatedPoint(oldLoc, dir, (point) -> SLData.get().getStatics().getStaticsAndDynamicsAtLocation(point));
            if(newLoc != null) {
                MoveRequestPacket packet = new MoveRequestPacket(dir, nextMoveSequence++, false);
                connection.sendPacket(packet);
                player.setLocation(newLoc);
                lastMoveTime = getTimeMillis();
            }
        }
        if(nextMoveSequence > 255) {
            nextMoveSequence = 0;
        }
    }

    public synchronized void allowMove(short sequence) {
        lastAckedMoveSequence = sequence;
    }

    public synchronized void denyMove(short deniedSequence, Point3D location, Direction facing) {
        nextMoveSequence = 0;
        lastAckedMoveSequence = 0;
        player.setLocation(location);
        player.setFacing(facing);
    }

    private synchronized void onPlayerLocationChange(Point3D oldLoc, Point3D newLoc) {
        checkInvisible();
    }

    private void checkInvisible() {
        // check for objects no longer visible
        Point3D location = player.getLocation();
        for(Iterator<SLObject> it = objectsInRange.values().iterator(); it.hasNext(); ) {
            SLObject obj = it.next();
            if(obj.getLocation().distanceTo(location) > updateRange) {
                it.remove();
            }
        }
    }

    public synchronized void updateOrInitObject(SendableObject object, Direction facing, int amount) {
        // valid in object: serial, graphic, location, hue
        SLObject updatedObj = objectsInRange.get(object.getSerial());
        if(updatedObj == null) {
            // init new object
            if(object.getSerial() >= Items.SERIAL_FIRST) {
                updatedObj = new SLItem(object.getSerial(), object.getGraphic());
            } else {
                updatedObj = new SLMobile(object.getSerial(), object.getGraphic());
            }
            objectsInRange.put(updatedObj.getSerial(), updatedObj);
        }
        updatedObj.setGraphic(object.getGraphic());
        updatedObj.setLocation(object.getLocation());
        updatedObj.setHue(object.getHue());
        if(updatedObj instanceof SLItem) {
            ((SLItem) updatedObj).setAmount(amount);
        } else if(updatedObj instanceof SLMobile) {
            ((SLMobile) updatedObj).setFacing(facing);
        }
        checkInvisible();
    }

    public synchronized void removeObject(long serial) {
        objectsInRange.remove(serial);
    }

    public synchronized void equipItem(SendableMobile mobInfo, SendableItem itemInfo) {
        SLObject obj;
        if(mobInfo.getSerial() == player.getSerial()) {
            obj = player;
        } else {
            obj = objectsInRange.get(mobInfo.getSerial());
            // if the item is visible or anything is known about it, forget it now
            objectsInRange.remove(itemInfo.getSerial());
        }

        if(obj == null || !(obj instanceof SLMobile)) {
            log.warning("Equip received for unknown serial " + String.format("%08X", obj.getSerial()));
            return;
        }
        SLMobile mob = (SLMobile) obj;
        SLItem itm = new SLItem(itemInfo.getSerial(), itemInfo.getGraphic());
        itm.setLayer(itemInfo.getLayer());
        itm.setHue(itemInfo.getHue());
        mob.equip(itm);
    }

    public long getTimeMillis() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
