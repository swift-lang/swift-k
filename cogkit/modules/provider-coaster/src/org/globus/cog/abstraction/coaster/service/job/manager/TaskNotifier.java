/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
