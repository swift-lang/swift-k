//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.ShutdownCommand;

public class Worker implements StatusListener {
    public static final Logger logger = Logger.getLogger(Worker.class);

    private static final Timer timer;

    static {
        timer = new Timer();
    }

    private static final Seconds SHUTDOWN_RESERVE = new Seconds(10);

    private Task task, running;
    private String id;
    private WorkerManager manager;
    private boolean starting;
    private Seconds scheduledTerminationTime;
    private Seconds maxWallTime;
    private Status error;
    private ChannelContext channelContext;

    public Worker(WorkerManager manager, String id, Seconds maxWallTime,
            Task w, Task p) {
        this.manager = manager;
        this.id = id;
        this.maxWallTime = maxWallTime;
        this.task = w;
        this.running = p;
        this.starting = true;
        this.scheduledTerminationTime = Seconds.NEVER;
        w.addStatusListener(this);
    }

    public void statusChanged(StatusEvent event) {
        Status s = event.getStatus();
        logger.warn("Worker " + id + " status change: " + s);
        int code = s.getStatusCode();
        Task src = (Task) event.getSource();
        if (code == Status.FAILED) {
            s.setMessage((s.getMessage() == null ? "" : s.getMessage())
                    + "\n" + src.getStdOutput() + "\n" + src.getStdError());
            error = s;
        }
        else if (code == Status.COMPLETED) {
            if (starting) {
                error = new StatusImpl(Status.FAILED,
                        "Worker ended prematurely", null);
            }
            else {
                error = new StatusImpl(Status.COMPLETED, "Worker exited",
                        null);
            }
        }
        else if (code == Status.CANCELED) {
            error = new StatusImpl(Status.FAILED, "Canceled", null);
        }
        if (s.isTerminal()) {
            manager.workerTerminated(this);
        }
    }

    public Task getWorkerTask() {
        return task;
    }

    public synchronized Task getRunning() {
        return running;
    }

    public void workerRegistered() {
        synchronized (this) {
            running = null;
            starting = false;
        }
    }

    public boolean isStarting() {
        return starting;
    }

    public String getId() {
        return id;
    }

    public Seconds getScheduledTerminationTime() {
        return scheduledTerminationTime;
    }

    public Seconds getMaxWallTime() {
        return maxWallTime;
    }

    public void setScheduledTerminationTime(Seconds s) {
        this.scheduledTerminationTime = s;
        shutdownAfter(s.add(SHUTDOWN_RESERVE).subtract(
                WorkerManager.TIME_RESERVE).toMilliseconds()
                - System.currentTimeMillis());
    }

    private void shutdownAfter(long ms) {
        timer.schedule(new TimerTask() {
            public void run() {
                if (running == null) {
                    shutdown();
                }
                else {
                    // what this really means is that a walltime spec was wrong
                    // and that the queuing system will likely kill the worker
                    // anyway
                    logger
                            .info("Worker still has a running task. Shutdown canceled.");
                }
            }
        }, ms);
    }

    public Status getStatus() {
        return error;
    }

    public String toString() {
        return "Worker[" + id + "]";
    }

    public void setChannelContext(ChannelContext cc) {
        this.channelContext = cc;
    }

    public ChannelContext getChannelContext() {
        return this.channelContext;
    }

    public void shutdown() {
        try {
            logger.info("Shutting down worker: " + this);
            KarajanChannel channel = ChannelManager.getManager()
                    .reserveChannel(channelContext);
            ShutdownCommand sc = new ShutdownCommand();
            sc.execute(channel);
            logger.info("Worker shut down: " + this);
        }
        catch (Exception e) {
            logger
                    .warn(
                            "Failed to shut down worker nicely. Trying to cancel task.",
                            e);
            try {
                manager.getTaskHandler().cancel(task);
                logger.info("Worker task canceled");
            }
            catch (Exception ee) {
                logger.info("Failed to cancel worker task", ee);
            }
        }
    }

    public boolean isPastScheduledTerminationTime() {
        return scheduledTerminationTime.toMilliseconds()
                - System.currentTimeMillis() < 0;
    }

    public synchronized void setRunning(Task task) {
        this.running = task;
        if (task == null && isPastScheduledTerminationTime()) {
            // that's to avoid running the shutdown in some strange thread it
            // shouldn't be running in
            shutdownAfter(1);
        }
    }
}
