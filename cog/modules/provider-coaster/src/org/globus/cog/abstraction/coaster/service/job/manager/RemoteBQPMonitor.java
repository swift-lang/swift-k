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
import org.globus.cog.abstraction.impl.file.coaster.commands.BQPStatusCommand;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class RemoteBQPMonitor implements BQPMonitor {
    public static final Logger logger = Logger.getLogger(RemoteBQPMonitor.class);

    private BlockQueueProcessor bqp;

    public RemoteBQPMonitor(BlockQueueProcessor bqp) {
        this.bqp = bqp;
    }

    public void update() {
        try {
            BQPStatusCommand bsc =
                    new BQPStatusCommand(bqp.getSettings(), bqp.getJobs(), bqp.getBlocks(),
                        bqp.getQueued());
            KarajanChannel channel =
                    ChannelManager.getManager().reserveChannel(bqp.getClientChannelContext());
            bsc.executeAsync(channel);
        }
        catch (Exception e) {
            logger.warn("Failed to send BQP updates", e);
        }
    }
}
