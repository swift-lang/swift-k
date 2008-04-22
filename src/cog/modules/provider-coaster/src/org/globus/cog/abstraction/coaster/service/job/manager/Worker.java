//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class Worker implements StatusListener {
    private Task task, running;
    private String id;
    private WorkerManager manager;
    private boolean starting;
    private Long scheduledTerminationTime;
    private int maxWallTime;
    private Status error;
    private ChannelContext channelContext;
    
    private static final Long NEVER = new Long(Long.MAX_VALUE);

    public Worker(WorkerManager manager, String id, int maxWallTime, Task w,
            Task p) {
        this.manager = manager;
        this.id = id;
        this.maxWallTime = maxWallTime;
        this.task = w;
        this.running = p;
        this.starting = true;
        this.scheduledTerminationTime = NEVER;
        w.addStatusListener(this);
    }

    public void statusChanged(StatusEvent event) {
        Status s = event.getStatus();
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

    public Task getRunning() {
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

    public Long getScheduledTerminationTime() {
        return scheduledTerminationTime;
    }

    public int getMaxWallTime() {
        return maxWallTime;
    }

    public void setScheduledTerminationTime(Long l) {
        this.scheduledTerminationTime = l;
    }

    public void setScheduledTerminationTime(long l) {
        this.scheduledTerminationTime = new Long(l);
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
}
