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
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.AbstractBlockWorkerManager;
import org.globus.cog.abstraction.impl.execution.coaster.WorkerShellCommand;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;
import org.globus.cog.coaster.handlers.RequestHandler;

public class WorkerShellHandler extends RequestHandler implements Callback {
    public static final Logger logger = Logger.getLogger(WorkerShellHandler.class);
    
    public static final String NAME = WorkerShellCommand.NAME;

    public void requestComplete() throws ProtocolException {
        String id = getInDataAsString(0);
        String command = getInDataAsString(1);
        WorkerShellCommand wsc = new WorkerShellCommand(id, command) {
            @Override
            public void handleSignal(byte[] data) {
                forwardSignal(data);
            }
        };
        CoasterChannel channel = getChannel();
        CoasterService service = (CoasterService) channel.getService();
        int sep = id.indexOf(':');
        String blockID = id.substring(0, sep);
        String workerID = id.substring(sep + 1);
        AbstractBlockWorkerManager bqp = service.getLocalService().getQueueProcessor(blockID);
        
        CoasterChannel worker = bqp.getWorkerChannel(blockID, workerID);
        if (worker == null) {
            sendReply("Error: worker not found");
        }
        else {
            wsc.executeAsync(channel, this);
        }
    }

    protected void forwardSignal(byte[] data) {
        this.getChannel().sendTaggedReply(getId(), data, CoasterChannel.SIGNAL_FLAG);
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        try {
            sendError("Worker error: " + msg, t);
        }
        catch (ProtocolException e) {
            logger.error("Cannot send reply: " + msg, e);
        }
    }

    public void replyReceived(Command cmd) {
        try {
            sendReply(cmd.getInDataAsString(1));
        }
        catch (ProtocolException e) {
            logger.error("Cannot send reply", e);
        }
    }
}
