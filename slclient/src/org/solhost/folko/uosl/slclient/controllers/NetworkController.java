package org.solhost.folko.uosl.slclient.controllers;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.network.SendableMobile;
import org.solhost.folko.uosl.libuosl.network.SendableObject;
import org.solhost.folko.uosl.libuosl.network.packets.AllowMovePacket;
import org.solhost.folko.uosl.libuosl.network.packets.DenyMovePacket;
import org.solhost.folko.uosl.libuosl.network.packets.EquipPacket;
import org.solhost.folko.uosl.libuosl.network.packets.InitPlayerPacket;
import org.solhost.folko.uosl.libuosl.network.packets.LocationPacket;
import org.solhost.folko.uosl.libuosl.network.packets.LoginErrorPacket;
import org.solhost.folko.uosl.libuosl.network.packets.RemoveObjectPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SLPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SendObjectPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SendTextPacket;
import org.solhost.folko.uosl.libuosl.network.packets.SoundPacket;
import org.solhost.folko.uosl.slclient.models.Connection;
import org.solhost.folko.uosl.slclient.models.GameState;
import org.solhost.folko.uosl.slclient.models.Player;
import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.models.Connection.ConnectionHandler;

public class NetworkController implements ConnectionHandler {
    private static final Logger log = Logger.getLogger("slclient.network");
    private final MainController mainController;
    private final GameState game;
    private Connection connection;

    public NetworkController(MainController mainController) {
        this.mainController = mainController;
        this.game = mainController.getGameState();
    }

    public void tryConnect(String host) {
        try {
            connection = new Connection(this, host);
        } catch (NumberFormatException e) {
            mainController.onNetworkError("Invalid port");
        } catch (UnresolvedAddressException e) {
            mainController.onNetworkError("Unknown host");
        } catch (IllegalArgumentException e) {
            mainController.onNetworkError("Invalid host");
        } catch (IOException e) {
            mainController.onNetworkError(e.getMessage());
        } catch (Exception e) {
            mainController.onNetworkError(e.getMessage());
        }
    }

    public void stopNetwork() {
        if(connection != null) {
            log.fine("Stopping network");
            connection.disconnect();
            game.onDisconnect();
            connection = null;
        }
    }

    @Override
    public void onConnected() {
        game.onConnect(connection);
    }

    @Override
    public void onNetworkError(String reason) {
        log.warning("Network error: " + reason);
        mainController.onNetworkError(reason);
        game.onDisconnect();
    }

    @Override
    public void onRemoteDisconnect() {
        log.warning("Remote disconnected");
        game.onDisconnect();
    }

    private void onLoginFail(LoginErrorPacket packet) {
        String message;
        switch(packet.getReason()) {
        case LoginErrorPacket.REASON_PASSWORD:
            message = "Invalid Password";
            break;
        case LoginErrorPacket.REASON_CHAR_NOT_FOUND:
            message = "Character not found";
            break;
        default:
            message = "Unknown reason";
            break;
        }
        mainController.onLoginFail(message);
    }

    private void onInitPlayer(InitPlayerPacket packet) {
        game.onLoginSuccess();
        game.getPlayer().setSerial(packet.getSerial());
    }

    private void onLocationChange(LocationPacket packet) {
        SendableMobile src = packet.getMobile();
        if(src.getSerial() != game.getPlayer().getSerial()) {
            log.warning("LocationPacket received for non-player object");
            return;
        }
        Player player = game.getPlayer();
        player.setGraphic(src.getGraphic());
        player.setFacing(src.getFacing());
        player.setLocation(src.getLocation());
    }

    private void onSendObject(SendObjectPacket packet) {
        game.updateOrInitObject(packet.getObject(), packet.getFacing(), packet.getAmount());
    }

    private void onRemoveObject(RemoveObjectPacket packet) {
        game.removeObject(packet.getSerial());
    }

    private void onEquip(EquipPacket packet) {
        game.equipItem(packet.getMobile(), packet.getItem());
    }

    private void onAllowMove(AllowMovePacket packet) {
        game.allowMove(packet.getSequence());
    }

    private void onDenyMove(DenyMovePacket packet) {
        game.denyMove(packet.getDeniedSequence(), packet.getLocation(), packet.getFacing());
    }

    private void onIncomingText(SendTextPacket packet) {
        SendableObject src = packet.getSource();
        SLObject obj = game.getObjectBySerial(src.getSerial());
        String text = packet.getText();
        long color = packet.getColor();

        switch(packet.getMode()) {
        case SendTextPacket.MODE_SAY:       mainController.incomingSay(obj, src.getName(), text, color); break;
        case SendTextPacket.MODE_SEE:       mainController.incomingSee(obj, src.getName(), text, color); break;
        case SendTextPacket.MODE_SYSMSG:    mainController.incomingSysMsg(text, color); break;
        default:
            log.warning("Unknown SendTextPacket mode: " + packet.getMode() + ", text: " + text);
        }
    }

    private void onSound(SoundPacket packet) {
        mainController.incomingSound(packet.getSoundID());
    }

    @Override
    public void onIncomingPacket(SLPacket packet) {
        log.finest("Incoming packet: " + packet);
        switch(packet.getID()) {
        case LoginErrorPacket.ID:   onLoginFail((LoginErrorPacket) packet); break;
        case InitPlayerPacket.ID:   onInitPlayer((InitPlayerPacket) packet); break;
        case SendTextPacket.ID:     onIncomingText((SendTextPacket) packet); break;
        case LocationPacket.ID:     onLocationChange((LocationPacket) packet); break;
        case SendObjectPacket.ID:   onSendObject((SendObjectPacket) packet); break;
        case RemoveObjectPacket.ID: onRemoveObject((RemoveObjectPacket) packet); break;
        case EquipPacket.ID:        onEquip((EquipPacket) packet); break;
        case AllowMovePacket.ID:    onAllowMove((AllowMovePacket) packet); break;
        case DenyMovePacket.ID:     onDenyMove((DenyMovePacket) packet); break;
        case SoundPacket.ID:        onSound((SoundPacket) packet); break;
        default:                    log.warning("Unknown packet: " + packet);
        }
    }
}
