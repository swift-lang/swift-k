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
 * Created on Oct 14, 2009
 */
package org.globus.cog.abstraction.coaster.rlog;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

public class RemoteLogger implements Callback {
    public static final Logger logger = Logger.getLogger(RemoteLogger.class);

    private ChannelContext ctx;

    public void setChannelContext(ChannelContext ctx) {
        this.ctx = ctx;
    }

    public void log(String msg) {
        if (ctx == null) {
            return;
        }
        RemoteLogCommand rlc = new RemoteLogCommand(msg);
        try {
            CoasterChannel channel = ChannelManager.getManager().reserveChannel(ctx);
            rlc.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.warn("Failed to send remote log message: " + msg, 
                        e);
        }
    }
    
    private void releaseChannel(Command cmd) {
        ChannelManager.getManager().releaseChannel(cmd.getChannel());
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        logger.warn("Failed to send command: " + msg, t);
        releaseChannel(cmd);
    }

    public void replyReceived(Command cmd) {
        releaseChannel(cmd);
    }
}
