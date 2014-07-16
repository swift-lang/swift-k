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
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

public class TaskNotifier implements StatusListener, ExtendedStatusListener, Callback {
    public static final Logger logger = Logger.getLogger(TaskNotifier.class);

    private ChannelContext channelContext;
    private Task task;
    private CoasterChannel channel;
    private static int notacknowledged;

    public TaskNotifier(Task task, ChannelContext channelContext) {
        this.task = task;
        this.channelContext = channelContext;
        this.task.addStatusListener(this);
        NotificationManager.getDefault().registerListener(task.getIdentity().getValue(), task, this);
    }
    
   
    public void statusChanged(StatusEvent event) {
        sendStatus(this, event.getStatus(), null, null);
    }

    public void statusChanged(Status s, String out, String err) {
        int code = s.getStatusCode();
        if (code != Status.SUBMITTED && code != Status.SUBMITTING) {
            sendStatus(this, s, out, err);
        }
    }

    public static synchronized void sendStatus(TaskNotifier tn, Status s, String out, String err) {
        String taskId = tn.task.getIdentity().toString();
        JobStatusCommand c = new JobStatusCommand(taskId, s, out, err);
        try {
            tn.channel = ChannelManager.getManager().reserveChannel(tn.channelContext);
            if (s.isTerminal()) {
                ChannelManager.getManager().releaseLongTerm(tn.channel);
            }
            c.executeAsync(tn.channel, tn);
            notacknowledged++;
        }
        catch (Exception e) {
            logger.warn("Failed to send task notification", e);
        }
    }
    
    public void errorReceived(Command cmd, String msg, Exception t) {
        logger.warn("Client could not properly process notification: " + msg, t);
        ChannelManager.getManager().releaseChannel(channel);
        synchronized(TaskNotifier.class) {
            notacknowledged--;
        }
    }

    public void replyReceived(Command cmd) {
        ChannelManager.getManager().releaseChannel(channel);
        synchronized(TaskNotifier.class) {
            notacknowledged--;
        }
    }
}
