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
import org.globus.cog.karajan.scheduler.LateBindingScheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.nodes.grid.SchedulerNode;
import org.griphyn.vdl.karajan.lib.VDLFunction;

public class HangChecker extends TimerTask {
    public static final Logger logger = Logger.getLogger(HangChecker.class);
    
    public static final int CHECK_INTERVAL = 10000;
    private Timer timer;
    private long lastEventCount;
    private WrapperMap map;
    private VariableStack stack;
    
    public HangChecker(VariableStack stack) throws ExecutionException {
        this.stack = stack;
        map = VDLFunction.getFutureWrapperMap(stack);
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
                    Monitor.dumpVariables(map, ps);
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
