package org.solhost.folko.uosl.slclient;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.common.LogFormatter;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.slclient.controllers.MainController;

import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class SLClient extends Application {
    private static final Logger log = Logger.getLogger("slclient");
    private MainController mainController;

    @Override
    public void start(Stage stage) {
        setupLogger(Level.FINER);

        log.info("Loading game data...");
        loadGameData();

        log.fine("Starting main controller");
        mainController = new MainController(stage);
        mainController.showLoginScreen();

        stage.setTitle("Ultima Online: Shattered Legacy");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if(mainController != null) {
            mainController.onGameWindowClosed();
        }
        super.stop();
    }

    private void loadGameData() {
        boolean dataLoaded = false;
        String dir = "data";
        do {
            try {
                SLData.init(dir);
                dataLoaded = true;
            } catch (IOException e) {
                DirectoryChooser cd = new DirectoryChooser();
                cd.setTitle("Choose UO:SL Data Directory");
                File newDir = cd.showDialog(null);
                if(newDir == null) {
                    System.exit(-1);
                }
                dir = newDir.toString();
            }
        } while(!dataLoaded);
    }

    private void setupLogger(Level level) {
        Handler handler = new ConsoleHandler();
        handler.setLevel(level);
        handler.setFormatter(new LogFormatter());

        log.setUseParentHandlers(false);
        log.addHandler(handler);
        log.setLevel(level);
        log.info("SLClient 0.0.2 starting...");
        log.info("Copyright 2014 by Folke Will");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
