//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.TaskHandler;

public class LocalQueueProcessor extends QueueProcessor {
    private TaskHandler taskHandler;

    public LocalQueueProcessor() {
        super("Local Queue Processor");
        this.taskHandler = new ExecutionTaskHandler();
    }

    public void run() {
        try {
            AssociatedTask at;
            while (!this.getShutdownFlag()) {
                at = take();
                try {
                    taskHandler.submit(at.task);
                }
                catch (Exception e) {
                	e.printStackTrace();
                	at.task.setStatus(new StatusImpl(Status.FAILED, null, e));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
