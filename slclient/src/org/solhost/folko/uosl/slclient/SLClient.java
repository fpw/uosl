package org.solhost.folko.uosl.slclient;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.solhost.folko.uosl.common.LogFormatter;
import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.slclient.controllers.MainController;

public class SLClient {
    private static final Logger log = Logger.getLogger("slclient");
    private final MainController mainController;

    public SLClient() {
        log.fine("Starting main controller");
        mainController = new MainController();
        mainController.showLoginScreen();
    }

    public void run() {
        mainController.gameLoop();
    }

    private static boolean initData() {
        try {
            SLData.init("data");
        } catch(Exception e) {
            return false;
        }
        SLData.get().buildCaches();
        return true;
    }

    private static void setupLogger(Level level) {
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
        setupLogger(Level.FINE);

        log.info("Loading game data...");
        if(!initData()) {
            log.severe("Couldn't load game data, make sure data/ directory exists and contains all MUL files with uppercase names");
            return;
        }

        SLClient client = new SLClient();
        client.run();
    }
}
