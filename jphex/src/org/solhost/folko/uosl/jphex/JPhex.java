/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.uosl.jphex;

import java.io.*;
import java.util.logging.*;

import org.solhost.folko.uosl.common.LogFormatter;
import org.solhost.folko.uosl.jphex.engines.TimerQueue;
import org.solhost.folko.uosl.jphex.network.PacketHandler;
import org.solhost.folko.uosl.jphex.network.Server;
import org.solhost.folko.uosl.jphex.scripting.ScriptAPI;
import org.solhost.folko.uosl.jphex.scripting.ScriptAPIImpl;
import org.solhost.folko.uosl.jphex.scripting.ScriptManager;
import org.solhost.folko.uosl.jphex.world.World;
import org.solhost.folko.uosl.libuosl.data.SLData;

public class JPhex {
    private static final Logger log = Logger.getLogger("jphex");
    private World world;
    private Server server;
    private Thread serverThread;
    private PacketHandler handler;
    private boolean stopped;

    public JPhex(Level logLevel) {
        Handler handler = new ConsoleHandler();
        handler.setLevel(logLevel);
        handler.setFormatter(new LogFormatter());

        log.setUseParentHandlers(false);
        log.addHandler(handler);
        log.setLevel(logLevel);
        log.info("JPhex 0.0.2 starting...");
        log.info("Copyright 2003-2004, 2013 by Folke Will");
    }

    public boolean loadData(String dataPath) {
        log.info("Loading client data... ");
        try {
            SLData.init(dataPath);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error reading client data: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    private boolean loadScripts(String path) {
        try {
            ScriptManager.init(path);
        } catch(Exception e) {
            log.log(Level.SEVERE, "Couldn't compile scripts: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public boolean loadWorld(String savePath) {
        try {
            log.info("Loading world... ");
            world = World.loadOrCreateNew(savePath);
            ScriptAPI api = new ScriptAPIImpl(world);
            ScriptManager.instance().setGlobal("$api", api);
            TimerQueue.start();
            world.init();
            return true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Couldn't initialize world: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean runServer(int port) {
        log.info("Starting network on port " + port + "... ");
        this.handler = new PacketHandler(world);

        server = new Server(port, handler);
        try {
            server.listen();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error listening: " + e.getMessage(), e);
            return false;
        }
        serverThread = new Thread() {
            @Override
            public void run() {
                server.eventLoop(new Server.ErrorHandler() {
                    public void onError(String message) {
                        log.severe("Fatal network error: " + message);
                        halt();
                    }
                });
            }
        };
        return true;
    }

    public void startEventLoop() {
        serverThread.start();
        log.info("JPhex running...");
    }

    public void save() {
        world.save();
    }

    public void halt() {
        log.info("Shutting down...");
        TimerQueue.stop();
        if(serverThread != null) {
            serverThread.interrupt();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                // don't care as we were killing it anyways
            }
        }
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public static void main(String[] args) throws IOException {
        JPhex phex = new JPhex(Level.INFO);
        if(!phex.loadData("data/")) {
            phex.halt();
            return;
        }
        if(!phex.loadScripts("scripts/")) {
            phex.halt();
            return;
        }
        if(!phex.loadWorld("saves/")) {
            phex.halt();
            return;
        }
        if(!phex.runServer(2590)) {
            phex.halt();
            return;
        }

        phex.startEventLoop();

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while(!phex.isStopped()) {
            String line = console.readLine();
            if(line == null) break;
            if(line.equals("save")) {
                phex.save();
            } else if(line.equals("quit")) {
                phex.save();
                phex.halt();
            } else if(line.equals("halt")) {
                phex.halt();
            }
        }
    }
}
