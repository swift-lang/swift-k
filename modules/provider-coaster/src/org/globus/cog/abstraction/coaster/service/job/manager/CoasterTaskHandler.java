//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.execution.coaster.SubmitJobCommand;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;

public class CoasterTaskHandler implements TaskHandler, Callback {
    public static final Logger logger = Logger
            .getLogger(CoasterTaskHandler.class);

    private WorkerManager workerManager;

    public CoasterTaskHandler(WorkerManager workerManager) {
        this.workerManager = workerManager;
    }

    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public int getType() {
        return EXECUTION;
    }

    public void setType(int type) {
    }

    public void remove(Task task) throws ActiveTaskException {
    }

    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        try {
            task.setStatus(Status.SUBMITTING);
            String contact = task.getService(0).getServiceContact()
                    .getContact();
            KarajanChannel channel = ChannelManager.getManager()
                    .reserveChannel(workerManager.getChannelContext(contact));
            ChannelManager.getManager().reserveLongTerm(channel);
            SubmitJobCommand cmd = new SubmitJobCommand(task);
            cmd.executeAsync(channel, this);
        }
        catch (Exception e) {
            throw new TaskSubmissionException(e);
        }
    }

    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    public Collection getActiveTasks() {
        throw new UnsupportedOperationException();
    }

    public Collection getAllTasks() {
        throw new UnsupportedOperationException();
    }

    public Collection getCanceledTasks() {
        throw new UnsupportedOperationException();
    }

    public Collection getCompletedTasks() {
        throw new UnsupportedOperationException();
    }

    public Collection getFailedTasks() {
        throw new UnsupportedOperationException();
    }

    public Collection getResumedTasks() {
        throw new UnsupportedOperationException();
    }

    public Collection getSuspendedTasks() {
        throw new UnsupportedOperationException();
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        SubmitJobCommand sjc = (SubmitJobCommand) cmd;
        Task task = sjc.getTask();
        task.setStatus(new StatusImpl(Status.FAILED, msg, t));
    }

    public void replyReceived(Command cmd) {
        SubmitJobCommand sjc = (SubmitJobCommand) cmd;
        Task task = sjc.getTask();
        task.setStatus(Status.SUBMITTED);
    }
}
