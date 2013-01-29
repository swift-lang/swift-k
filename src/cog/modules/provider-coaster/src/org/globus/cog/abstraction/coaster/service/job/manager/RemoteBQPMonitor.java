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
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
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
            CoasterChannel channel =
                    ChannelManager.getManager().reserveChannel(bqp.getClientChannelContext());
            bsc.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.warn("Failed to send BQP updates", e);
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
