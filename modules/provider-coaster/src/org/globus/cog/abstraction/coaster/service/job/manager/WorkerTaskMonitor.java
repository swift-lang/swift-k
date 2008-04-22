//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 12, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;

public class WorkerTaskMonitor implements StatusListener {
    private Worker wr;
    private WorkerManager workerManager;

    public WorkerTaskMonitor(Task task, WorkerManager workerManager, Worker wr) {
        this.workerManager = workerManager;
        this.wr = wr;
        task.addStatusListener(this);
    }

    public void statusChanged(StatusEvent event) {
        Status s = event.getStatus();
        if (s.isTerminal()) {
            workerManager.workerTaskDone(wr);
        }
    }

}
