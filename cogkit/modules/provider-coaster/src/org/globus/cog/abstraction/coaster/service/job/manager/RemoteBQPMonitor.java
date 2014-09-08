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
 * Created on May 18, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

public class RemoteBQPMonitor implements BQPMonitor, Callback {
    public static final Logger logger = Logger.getLogger(RemoteBQPMonitor.class);

    private BlockQueueProcessor bqp;

    public RemoteBQPMonitor(BlockQueueProcessor bqp) {
        this.bqp = bqp;
    }

    public void update() {
        try {
            BQPStatusCommand bsc =
                    new BQPStatusCommand(bqp.getSettings(), bqp.getJobs(), bqp.getBlocks().values(),
                        bqp.getQueued());
            bqp.getBroadcaster().send(bsc);
        }
        catch (Exception e) {
            logger.warn("Failed to send BQP updates", e);
        }
    }
    
    public void errorReceived(Command cmd, String msg, Exception t) {
        logger.warn("Failed to send command: " + msg, t);
    }

    public void replyReceived(Command cmd) {
    }
}
