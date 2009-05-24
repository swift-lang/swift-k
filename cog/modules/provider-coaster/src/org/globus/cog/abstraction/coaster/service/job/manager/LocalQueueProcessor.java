//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class LocalQueueProcessor extends AbstractQueueProcessor {
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
                    at.task.setService(0, buildService(at.task));
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

    public void setClientChannelContext(ChannelContext channelContext) {
    }
    
    public static ExecutionService buildService(Task prototype)
            throws InvalidServiceContactException, InvalidProviderException,
            ProviderMethodException {
        ExecutionService s = new ExecutionServiceImpl();
        s.setServiceContact(prototype.getService(0).getServiceContact());
        ExecutionService p = (ExecutionService) prototype.getService(0);
        String jm = p.getJobManager();
        int colon = jm.indexOf(':');
        // remove provider used to bootstrap coasters
        jm = jm.substring(colon + 1);
        colon = jm.indexOf(':');
        if (colon == -1) {
            s.setProvider(jm);
        }
        else {
            s.setJobManager(jm.substring(colon + 1));
            s.setProvider(jm.substring(0, colon));
        }
        if (p.getSecurityContext() != null) {
            s.setSecurityContext(p.getSecurityContext());
        }
        else {
            s.setSecurityContext(AbstractionFactory.newSecurityContext(s.getProvider()));
        }
        return s;
    }
}
