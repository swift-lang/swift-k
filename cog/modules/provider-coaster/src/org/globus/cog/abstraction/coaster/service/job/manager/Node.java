//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 16, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.ShutdownCommand;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;

public class Node implements Callback {
    public static final Logger logger = Logger.getLogger(Node.class);

    private final int id;
    private final List<Cpu> cpus;
    private Block block;
    private final ChannelContext channelContext;
    private boolean shutdown;
    private final String hostname;

    public Node(int id, Block block, String workerHostname,
                ChannelContext channelContext) {
        this.id = id;
        this.block = block;
        this.hostname = workerHostname;
        this.channelContext = channelContext;
        Settings settings = block.getAllocationProcessor().getSettings();
        cpus = new ArrayList<Cpu>(settings.getJobsPerNode());

        logger.debug("new: " + this);
    }

    @Deprecated
    public Node(int id, Block block, ChannelContext channelContext) {
        this.id = id;
        this.block = block;
        this.hostname = null;
        this.channelContext = channelContext;
        Settings settings = block.getAllocationProcessor().getSettings();
        cpus = new ArrayList<Cpu>(settings.getJobsPerNode());
    }

    @Deprecated
    public Node(int id, int jobsPerNode, ChannelContext channelContext) {
    	this.id = id;
    	this.hostname = null;
    	this.channelContext = channelContext;
    	cpus = new ArrayList<Cpu>(jobsPerNode);
    }

    public void add(Cpu cpu) {
        cpus.add(cpu);
    }

    public Block getBlock() {
        return block;
    }

    public void shutdown() {
        synchronized(this) {
            if (shutdown) {
                return;
            }
            else {
                shutdown = true;
            }
        }
        try {
            KarajanChannel channel = ChannelManager.getManager().reserveChannel(channelContext);
            ChannelManager.getManager().reserveLongTerm(channel);
            ShutdownCommand cmd = new ShutdownCommand();
            cmd.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.info("Failed to shut down worker", e);
            block.forceShutdown();
        }
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        logger.info("Failed to shut down " + this + ": " + msg, t);
        block.forceShutdown();
    }

    public void replyReceived(Command cmd) {
        logger.info(this + " shut down successfully");
    }

    public ChannelContext getChannelContext() {
        return channelContext;
    }

    @Override
    public String toString() {
        return "Node [" + hostname + "] " + id;
    }
}
