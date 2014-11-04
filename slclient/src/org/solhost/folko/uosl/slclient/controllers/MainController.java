package org.solhost.folko.uosl.slclient.controllers;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.slclient.models.GameState;
import org.solhost.folko.uosl.slclient.models.SLItem;
import org.solhost.folko.uosl.slclient.models.SLMobile;
import org.solhost.folko.uosl.slclient.models.SLObject;
import org.solhost.folko.uosl.slclient.models.TexturePool;
import org.solhost.folko.uosl.slclient.models.GameState.State;
import org.solhost.folko.uosl.slclient.views.GameView;
import org.solhost.folko.uosl.slclient.views.LoginDialog;
import org.solhost.folko.uosl.slclient.views.MainView;
import org.solhost.folko.uosl.slclient.views.SoundManager;

public class MainController {
    private static final Logger log = Logger.getLogger("slclient.main");
    private final GameState game;
    private final NetworkController networkController;
    private final SoundManager soundManager;

    private boolean gameLoopRunning;
    private final MainView mainView;
    private final LoginDialog loginView;
    private final GameView gameView;

    public MainController() {
        this.game = new GameState();
        this.networkController = new NetworkController(this);
        this.soundManager = new SoundManager(game);
        this.mainView = new MainView(this);

        gameView = mainView.getGameView();
        loginView = mainView.getLoginView();

        game.addStateListener(this::onGameStateChange);
        mainView.setVisible(true);
    }

    public void showLoginScreen() {
        mainView.showLoginDialog();
    }

    private void onGameStateChange(State oldState, State newState) {
        log.fine("Game state changed from " + oldState + " to " + newState);
        switch(newState) {
        case DISCONNECTED:
            onDisconnect(oldState);
            break;
        case CONNECTED:
            onConnected(oldState);
            break;
        case LOGGED_IN:
            onLogin(oldState);
            break;
        default:
            break;
        }
    }

    // user entered login data
    public void onLoginRequest(String host, String name, String password) {
        loginView.setBusy(true);
        game.setLoginDetails(name, password);
        networkController.tryConnect(host);
    }

    private void onDisconnect(GameState.State oldState) {
        if(oldState == State.CONNECTED) {
            // login view still active, but server kicked us
            loginView.setBusy(false);
        }
    }

    private void onConnected(GameState.State oldState) {
        log.info("Connected to server");
        game.tryLogin();
    }

    public void onLoginFail(String message) {
        networkController.stopNetwork();
        loginView.showError("Login failed: " + message);
        loginView.setBusy(false);
    }

    private void onLogin(State oldState) {
        loginView.close();
    }

    public void onNetworkError(String reason) {
        if(game.getState() == State.DISCONNECTED || game.getState() == State.CONNECTED) {
            // means we couldn't connect or were kicked while logging in
            loginView.showError("Couldn't connect: " + reason);
            loginView.setBusy(false);
        }
    }

    public void onGameError(String reason) {
        log.severe("Game error: " + reason);
        onGameWindowClosed();
    }

    // can be called from Swing thread or game thread
    public synchronized void onGameWindowClosed() {
        if(gameLoopRunning) {
            log.fine("Stopping game...");
            gameLoopRunning = false;
        }
        mainView.close();
    }

    // this is the main game thread, everything in GameView and GameState should run
    // in this thread. Events can be posted using scheduleUpdate
    public void gameLoop() {
        long lastFrameTime = game.getTimeMillis();
        long thisFrameTime = game.getTimeMillis();
        gameLoopRunning = true;

        try {
            startSystems();
            log.fine("Entering game loop");
            while(gameLoopRunning) {
                    thisFrameTime = game.getTimeMillis();
                    update(thisFrameTime - lastFrameTime);
                    gameView.render();
                    gameView.pause();
                    lastFrameTime = thisFrameTime;
            }
        } catch(Exception e) {
            log.log(Level.SEVERE, "Game crashed: " + e.getMessage(), e);
            onGameError("Game crashed: " + e.getMessage());
        }
        log.fine("Left game loop");
        shutdownSystems();
    }

    private void startSystems() throws Exception {
        gameView.init();
        soundManager.init();
        TexturePool.load();
    }

    private void shutdownSystems() {
        soundManager.stop();
        networkController.stopNetwork();
    }

    private void update(long elapsedMillis) {
        try {
            networkController.update(elapsedMillis);
            gameView.update(elapsedMillis);
            soundManager.update(elapsedMillis);
        } catch(Exception e) {
            log.log(Level.SEVERE, "Exception in game: " + e.getMessage(), e);
            onGameError("Game has crashed: " + e.getMessage());
            return;
        }
    }

    public void onRequestMove(Direction dir) {
        game.playerMoveRequest(dir);
    }

    public void onUpdateRangeChange(int sceneRadius) {
        game.setUpdateRange(sceneRadius);
    }

    public void onTextEntered(String text) {
        if(text.length() > 0) {
            game.playerTextInput(text);
        }
    }

    public void onSingleClickObject(SLObject obj) {
        if(obj instanceof SLItem) {
            String name = obj.getName();
            if(name.length() > 0) {
                gameView.showTextAbove(obj, name, Color.WHITE);
            }
        } else if(obj instanceof SLMobile) {
            game.queryMobileInformation((SLMobile) obj);
        }
    }

    public void onDoubleClickObject(SLObject obj) {
        game.doubleClick(obj);
    }

    public void incomingSysMsg(String text, long color) {
        log.fine("SysMessage: " + text);
        int col = Integer.reverseBytes((int) color) >> 8;
        gameView.showSysMessage(text, new Color(col));
    }

    public void incomingSee(SLObject obj, String name, String text, long color) {
        if(obj == null) {
            log.warning("Received 'you see' message for unknown object");
            return;
        }
        // TODO: log to journal with name
        int col = Integer.reverseBytes((int) color) >> 8;
        gameView.showTextAbove(obj, text, new Color(col));
    }

    public void incomingSpeech(SLObject obj, String name, String text, long color) {
        if(obj == null) {
            // TODO: display in lower left or something
            log.warning("Received speech for unknown object");
            return;
        }
        // TODO: log to journal with name
        int col = Integer.reverseBytes((int) color) >> 8;
        gameView.showTextAbove(obj, text, new Color(col));
    }

    public void incomingSound(int soundID) {
        soundManager.playSound(soundID);
    }

    public void onReportFPS(long fps) {
        mainView.setTitleSuffix("| " + fps + " FPS");
    }

    public GameState getGameState() {
        return game;
    }
}
