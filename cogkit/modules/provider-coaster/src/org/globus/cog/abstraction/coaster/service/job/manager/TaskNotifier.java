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
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

public class TaskNotifier implements StatusListener, ExtendedStatusListener, Callback {
    public static final Logger logger = Logger.getLogger(TaskNotifier.class);

    private Task task;
    private String clientTaskId;
    private CoasterChannel channel;
    private static int notacknowledged;

    public TaskNotifier(Task task, String clientTaskId, CoasterChannel channel) {
        this.task = task;
        this.clientTaskId = clientTaskId;
        if (logger.isInfoEnabled()) {
            logger.info("Task id mapping: " + channel + ":" + clientTaskId + " -> " + task.getIdentity());
        }
        this.channel = channel;
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

    public static void sendStatus(TaskNotifier tn, Status s, String out, String err) {
        JobStatusCommand c = new JobStatusCommand(tn.clientTaskId, s, out, err);
        try {
            c.executeAsync(tn.channel, tn);
            synchronized(TaskNotifier.class) {
                notacknowledged++;
            }
        }
        catch (Exception e) {
            logger.warn("Failed to send task notification for " + tn.task.getIdentity(), e);
        }
    }
    
    public void errorReceived(Command cmd, String msg, Exception t) {
        logger.info("Client could not properly process notification for " + task.getIdentity() + ": " + 
            msg + ". Command was " + cmd, t);
        synchronized(TaskNotifier.class) {
            notacknowledged--;
        }
    }

    public void replyReceived(Command cmd) {
        synchronized(TaskNotifier.class) {
            notacknowledged--;
        }
        logger.info("Reply received for " + cmd);
    }
}
