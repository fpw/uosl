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
package org.solhost.folko.uosl.jphex.engines;

import java.util.concurrent.DelayQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimerQueue {
    private final DelayQueue<Timer> timers;
    private static final Logger log = Logger.getLogger("jphex.timerqueue");
    private static TimerQueue instance;
    private final Thread timerThread;
    private boolean wantStop;

    private TimerQueue() {
        this.timers = new DelayQueue<Timer>();
        this.timerThread = getTimerThread();
    }

    public static TimerQueue get() {
        if(instance == null) {
            log.severe("access to timer queue before init");
        }
        return instance;
    }

    public static void start() {
        if(instance != null) {
            log.severe("timer queue initialized twice");
            return;
        }
        instance = new TimerQueue();
        instance.startTimerThread();
    }

    public static void stop() {
        if(instance == null) {
            return;
        }
        instance.wantStop = true;
        if(instance.timerThread != null) {
            instance.timerThread.interrupt();
        }
        try {
            instance.timerThread.join();
        } catch (InterruptedException e) {
            // doesn't matter as we're stopping anyways
        }
    }

    public void addTimer(Timer timer) {
        timers.add(timer);
    }

    private void timerLoop() {
        log.fine("TimerQueue active");
        while(!wantStop) {
            try {
                Timer first = timers.take();
                first.run();
            } catch (InterruptedException e) {
                if(wantStop) {
                    break;
                }
            } catch(Exception e) {
                log.log(Level.SEVERE, "Exception in timer: " + e, e);
            }
        }
        log.fine("TimerQueue inactive");
    }

    private Thread getTimerThread() {
        return new Thread() {
            @Override
            public void run() {
                timerLoop();
            }
        };
    }

    private void startTimerThread() {
        timerThread.start();
    }
}
