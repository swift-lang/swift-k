//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.JobStatusCommand;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;

public class TaskNotifier implements StatusListener, Callback {
    public static final Logger logger = Logger.getLogger(TaskNotifier.class);

    private ChannelContext channelContext;
    private Task task;
    private KarajanChannel channel;

    public TaskNotifier(Task task, ChannelContext channelContext) {
        this.task = task;
        this.channelContext = channelContext;
        task.addStatusListener(this);
        NotificationManager.getDefault().registerTask(
                    task.getIdentity().getValue(), task);
    }

    public void statusChanged(StatusEvent event) {
        int code = event.getStatus().getStatusCode();
        if (code != Status.SUBMITTED && code != Status.SUBMITTING) {
            sendStatus(event.getStatus());
        }
    }

    private void sendStatus(Status s) {
        String taskId = task.getIdentity().toString();
        JobStatusCommand c = new JobStatusCommand(taskId, s);
        try {
            channel = ChannelManager.getManager()
                    .reserveChannel(channelContext);
            if (s.isTerminal()) {
                ChannelManager.getManager().releaseLongTerm(channel);
            }
            c.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.warn("Failed to send task notification", e);
        }
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
    	logger.warn("Client could not properly process notification: " + msg, t);
    	ChannelManager.getManager().releaseChannel(channel);
    }

    public void replyReceived(Command cmd) {
        ChannelManager.getManager().releaseChannel(channel);
    }
}
