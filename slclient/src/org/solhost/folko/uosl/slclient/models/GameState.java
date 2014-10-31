package org.solhost.folko.uosl.slclient.models;

import java.util.logging.Logger;
import java.util.stream.Stream;

import org.lwjgl.Sys;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.network.SendableItem;
import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.network.SendableObject;
import org.solhost.folko.uosl.libuosl.network.packets.DoubleClickPacket;
import org.solhost.folko.uosl.libuosl.network.packets.LoginPacket;
import org.solhost.folko.uosl.libuosl.network.packets.MoveRequestPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SingleClickPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SpeechRequestPacket;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Items;
import org.solhost.folko.uosl.libuosl.types.Point2D;
import org.solhost.folko.uosl.libuosl.types.Point3D;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

public class GameState {
    public enum State {DISCONNECTED, CONNECTED, LOGGED_IN};

    private static final Logger log = Logger.getLogger("slclient.game");
    private final Player player;
    private final Property<State> state;
    private final ObjectRegistry objectsInRange;

    private Connection connection;
    private int updateRange = 15;


    // Movement
    private final int MOVE_DELAY = 150;
    private short nextMoveSequence, lastAckedMoveSequence;
    private long lastMoveTime;

    public GameState() {
        state = new SimpleObjectProperty<GameState.State>(State.DISCONNECTED);
        objectsInRange = new ObjectRegistry();
        player = new Player(-1, -1);

        player.locationProperty().addListener((Player, oldLoc, newLoc) -> onPlayerLocationChange(oldLoc, newLoc));

        log.fine("Game state initialized");
    }

    public ReadOnlyProperty<State> stateProperty() {
        return state;
    }

    public State getState() {
        return state.getValue();
    }

    public Player getPlayer() {
        return player;
    }

    public void onConnect(Connection connection) {
        this.connection = connection;
        state.setValue(State.CONNECTED);
    }

    public void onDisconnect() {
        this.connection = null;
        state.setValue(State.DISCONNECTED);
    }

    public void tryLogin() {
        LoginPacket login = new LoginPacket();
        login.setName(player.getName());
        login.setPassword(player.getPassword());
        login.setSeed(LoginPacket.LOGIN_BY_NAME);
        login.setSerial(LoginPacket.LOGIN_BY_NAME);
        login.prepareSend();
        connection.sendPacket(login);
    }

    public void onLoginSuccess() {
        state.setValue(State.LOGGED_IN);
        objectsInRange.registerObject(player);
    }

    public int getUpdateRange() {
        return updateRange;
    }

    public void setUpdateRange(int updateRange) {
        this.updateRange = updateRange;
        if(player.getLocation() != null) {
            // treat as location change to remove old and show new objects
            onPlayerLocationChange(player.getLocation(), player.getLocation());
        }
    }

    // there is potential for optimization here
    public Stream<SLObject> getObjectsAt(Point2D point) {
        return Stream.concat(
                objectsInRange.getObjectsAt(point),
                SLData.get().getStatics().getStaticsStream(point)
                    .map((sta) -> SLItem.fromStatic(sta)));
    }

    public void playerMoveRequest(Direction dir) {
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
            Point3D newLoc = SLData.get().getElevatedPoint(oldLoc, dir, (point) -> SLData.get().getStatics().getStatics(point));
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

    public void playerTextInput(String text) {
        SpeechRequestPacket packet = new SpeechRequestPacket(text, 0x0000FF, SpeechRequestPacket.MODE_BARK);
        connection.sendPacket(packet);
    }

    public void allowMove(short sequence) {
        lastAckedMoveSequence = sequence;
    }

    public void denyMove(short deniedSequence, Point3D location, Direction facing) {
        nextMoveSequence = 0;
        lastAckedMoveSequence = 0;
        player.setLocation(location);
        player.setFacing(facing);
    }

    private void onPlayerLocationChange(Point3D oldLoc, Point3D newLoc) {
        removeOutOfRangeObjects();
    }

    private void removeOutOfRangeObjects() {
        // remove objects that are no longer visible
        objectsInRange.removeFarther(player.getLocation(), updateRange);
    }

    public void updateOrInitObject(SendableObject object, Direction facing, int amount) {
        // valid in object: serial, graphic, location, hue
        SLObject updatedObj = objectsInRange.getObjectBySerial(object.getSerial());
        if(updatedObj == null) {
            // init new object
            if(object.getSerial() >= Items.SERIAL_FIRST) {
                updatedObj = new SLItem(object.getSerial(), object.getGraphic());
            } else {
                updatedObj = new SLMobile(object.getSerial(), object.getGraphic());
            }
            objectsInRange.registerObject(updatedObj);
        }
        updatedObj.setGraphic(object.getGraphic());
        updatedObj.setLocation(object.getLocation());
        updatedObj.setHue(object.getHue());
        if(updatedObj instanceof SLItem) {
            ((SLItem) updatedObj).setAmount(amount);
        } else if(updatedObj instanceof SLMobile) {
            ((SLMobile) updatedObj).setFacing(facing);
        }
        removeOutOfRangeObjects();
    }

    public void queryMobileInformation(SLMobile mob) {
        SingleClickPacket packet = new SingleClickPacket(mob.getSerial());
        connection.sendPacket(packet);
    }

    public void doubleClick(SLObject obj) {
        DoubleClickPacket packet = new DoubleClickPacket(obj.getSerial());
        connection.sendPacket(packet);
    }

    public void equipItem(SendableMobile mobInfo, SendableItem itemInfo) {
        SLObject obj = objectsInRange.getObjectBySerial(mobInfo.getSerial());
        // if the item is visible or anything is known about it, forget it now
        objectsInRange.removeObject(itemInfo.getSerial());

        if(obj == null || !(obj instanceof SLMobile)) {
            log.fine("Equip received for unknown serial " + String.format("%08X", mobInfo.getSerial()));
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

    public void removeObject(long serial) {
        objectsInRange.removeObject(serial);
    }

    public SLObject getObjectBySerial(long serial) {
        return objectsInRange.getObjectBySerial(serial);
    }
}
