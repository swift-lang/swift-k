//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.fake;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler {
    private static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

    public static volatile int jobsRun;
    
    private static final LinkedBlockingQueue<Task> ender;
    
    static {
        ender = new LinkedBlockingQueue<Task>();
        new Thread() {
            {
                setName("Fake provider");
            }
            
            public void run() {
                while (true) {
                    try {
                        Task t = ender.take();
                        jobsRun++;
                        t.setStatus(Status.COMPLETED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    
    public void submit(final Task task) throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        checkAndSetTask(task);
        task.setStatus(Status.SUBMITTING);
        JobSpecification spec;
        try {
            spec = (JobSpecification) task.getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException("Exception while retrieving Job Specification", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(spec.toString());
        }

        try {
            int delay = 0;
            synchronized (this) {
                if (task.getStatus().getStatusCode() != Status.CANCELED) {
                    task.setStatus(Status.SUBMITTED);
                    if (spec.isBatchJob()) {
                        task.setStatus(Status.COMPLETED);
                    }
                    else {
                        ender.put(task);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Cannot submit job", e);
        }
    }

    public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
    }

    public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
    }

    public void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        synchronized (this) {
            getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
        }
    }
}
