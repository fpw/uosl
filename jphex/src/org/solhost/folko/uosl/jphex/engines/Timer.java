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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Timer implements Delayed {
    private final Runnable what;
    private final long delay;
    private long when;

    // call in n milliseconds
    public Timer(long milliseconds, Runnable what) {
        this.delay = milliseconds;
        this.when = getCurrentTicks() + delay;
        this.what = what;
    }

    // base reference for timers
    public static long getCurrentTicks() {
        return System.currentTimeMillis();
    }

    public void reset() {
        this.when = getCurrentTicks() + delay;
    }

    public void run() {
        what.run();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delta = when - getCurrentTicks();
        return unit.convert(delta, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long delta = this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
        if(delta > 0) {
            // We should expire later
            return 1;
        } else if(delta < 0) {
            // We should expire sooner
            return -1;
        } else {
            return 0;
        }
    }
}
