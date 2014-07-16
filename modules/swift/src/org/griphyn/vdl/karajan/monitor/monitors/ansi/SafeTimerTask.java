//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 30, 2012
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Screen;

public abstract class SafeTimerTask extends TimerTask {
    public static final Logger logger = Logger.getLogger(SafeTimerTask.class);
    private Screen screen;
    
    public SafeTimerTask() {
    }
    
    public SafeTimerTask(Screen screen) {
        this.screen = screen;
    }
    
    @Override
    public final void run() {
        try {
            runTask();
        }
        catch (Exception e) {
            if (screen != null) {
                CharArrayWriter caw = new CharArrayWriter();
                e.printStackTrace(new PrintWriter(caw));
                Dialog.displaySimpleDialog(screen, "Error", caw.toString(), new String[] {"Close"});
            }
            else {
                logger.warn("Exception in timer task", e);
            }
        }
    }
    
    public abstract void runTask();
}
