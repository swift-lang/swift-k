/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
