/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.karajan;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.scheduler.LateBindingScheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.nodes.grid.SchedulerNode;

public class HangChecker extends TimerTask {
    public static final Logger logger = Logger.getLogger(HangChecker.class);
    
    public static final int CHECK_INTERVAL = 10000;
    private Timer timer;
    private long lastEventCount;
    private VariableStack stack;
    
    public HangChecker(VariableStack stack) throws ExecutionException {
        this.stack = stack;
    }

    public void start() {
        timer = new Timer("Hang checker");
        timer.scheduleAtFixedRate(this, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    public void run() {
        try {
            LateBindingScheduler s = (LateBindingScheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
            if (s != null) {
                int running = s.getRunning();

                if (running == 0 && EventBus.eventCount == lastEventCount) {
                    logger.warn("No events in " + (CHECK_INTERVAL / 1000) + "s.");
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(os);
                    Monitor.dumpVariables(ps);
                    Monitor.dumpThreads(ps);
                    logger.warn(os.toString());
                    ps.close();
                }
            }
            lastEventCount = EventBus.eventCount;
        }
        catch (Exception e) {
            logger.warn("Exception caught during hang check", e);
        }
    }
}
