package org.solhost.folko.uosl.slclient.controllers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.slclient.models.GameState;
import org.solhost.folko.uosl.slclient.models.GameState.State;
import org.solhost.folko.uosl.slclient.views.GameView;
import org.solhost.folko.uosl.slclient.views.LoginView;
import org.solhost.folko.uosl.slclient.views.SoundManager;

import javafx.application.Platform;
import javafx.stage.Stage;

public class MainController {
    private static final Logger log = Logger.getLogger("slclient.main");
    private final GameState game;
    private final NetworkController networkController;
    private final Stage stage;
    private Thread gameThread;
    private boolean gameRunning;
    private LoginView loginView;
    private GameView gameView;
    private final SoundManager soundManager;

    public MainController(Stage stage) {
        this.stage = stage;
        this.game = new GameState();
        this.networkController = new NetworkController(this);
        this.soundManager = new SoundManager(game);

        game.stateProperty().addListener((g, from, to) -> onGameStateChange(from, to));
    }

    public void showLoginScreen() {
        loginView = new LoginView(this);
        stage.setScene(loginView.getScene());
        stage.show();
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
        game.getPlayer().setName(name);
        game.getPlayer().setPassword(password);
        loginView.setBusy(true);
        networkController.tryConnect(host);
    }

    private void onDisconnect(GameState.State oldState) {
        if(oldState == State.CONNECTED) {
            // login view still active, but server kicked us
            Platform.runLater(() -> {
                loginView.setBusy(false);
            });
        }
    }

    private void onConnected(GameState.State oldState) {
        log.info("Connected to server");
        game.tryLogin();
    }

    public void onLoginFail(String message) {
        networkController.stopNetwork();
        Platform.runLater(() -> {
            loginView.showError("Login failed: " + message);
            loginView.setBusy(false);
        });
    }

    private void onLogin(State oldState) {
        // stop JavaFX, start LWJGL
        FutureTask<Void> task = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            stage.hide();
        }, null);

        if(Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }

        try {
            task.get(); // wait for task to complete
            gameView = new GameView(this);
            gameThread = new Thread(() -> gameLoop());
            gameThread.start();
        } catch (InterruptedException | ExecutionException e) {
            log.log(Level.FINE, "Login stopped: " + e.getMessage(), e);
        }
    }

    public void onNetworkError(String reason) {
        if(game.getState() == State.DISCONNECTED || game.getState() == State.CONNECTED) {
            // means we couldn't connect or were kicked while logging in
            Platform.runLater(() -> {
                loginView.showError("Couldn't connect: " + reason);
                loginView.setBusy(false);
            });
        }
    }

    public void onGameError(String reason) {
        log.severe("Game error: " + reason);
    }

    public void onGameWindowClosed() {
        if(gameRunning) {
            log.fine("Stopping game...");
            gameRunning = false;
        }
        shutdownSystems();
    }

    private void gameLoop() {
        long lastFrameTime = game.getTimeMillis();
        long thisFrameTime = game.getTimeMillis();
        gameRunning = true;

        gameView.createWindow();
        try {
            while(gameRunning) {
                    thisFrameTime = game.getTimeMillis();
                    update(thisFrameTime - lastFrameTime);
                    gameView.render();
                    lastFrameTime = thisFrameTime;
                    gameView.pause();
            }
        } catch(Exception e) {
            log.log(Level.SEVERE, "Game crashed: " + e.getMessage(), e);
            onGameError("Game crashed: " + e.getMessage());
        }
        gameRunning = false;
        gameView.close();
        shutdownSystems();
        Platform.exit();
    }

    private void shutdownSystems() {
        soundManager.stop();
        networkController.stopNetwork();
    }

    private void update(long elapsedMillis) {
        try {
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

    public void onReportFPS(long fps) {
        log.finest("FPS: " + fps);
    }

    public Stage getStage() {
        return stage;
    }

    public GameState getGameState() {
        return game;
    }
}
