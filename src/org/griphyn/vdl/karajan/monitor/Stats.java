//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.Timer;
import java.util.TimerTask;

public class Stats {
    public static final int PERIOD = 1000;
    
    private static Timer timer = new Timer();
    
    public Stats() {
        timer.schedule(new TimerTask() {
            public void run() {
                period = 0;
            }}, PERIOD, PERIOD);
    }
    
    private int total, current, period;
    
    public synchronized void add() {
        total++;
        current++;
        period++;
    }
    
    public synchronized void remove() {
        current--;
    }
    
    public int getTotal() {
        return total;
    }
    
    public int getCurrent() {
        return current;
    }
    
    public int getPeriod() {
        return period;
    }
}
