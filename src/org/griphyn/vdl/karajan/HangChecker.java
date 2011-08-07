//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 6, 2011
 */
package org.griphyn.vdl.karajan;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
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
            WeightedHostScoreScheduler s = (WeightedHostScoreScheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
            if (s != null) {
                int running = s.getRunning();
                boolean allOverloaded = s.allOverloaded();
                if (running == 0 && EventBus.eventCount == lastEventCount && !allOverloaded) {
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
