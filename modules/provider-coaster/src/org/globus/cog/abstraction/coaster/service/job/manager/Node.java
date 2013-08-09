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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.ChannelException;
import org.globus.cog.coaster.channels.ChannelListener;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.ShutdownCommand;
import org.globus.cog.coaster.commands.Command.Callback;

public class Node implements Callback, ChannelListener {
    public static final Logger logger = Logger.getLogger(Node.class);

    private final int id;
    private final List<Cpu> cpus;
    private Block block;
    private final ChannelContext channelContext;
    private CoasterChannel channel;
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

        if (logger.isDebugEnabled()) {
            logger.debug("new: " + this);
        }
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
            CoasterChannel channel = getChannel();
            channel.setLocalShutdown();
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
        try {
        	getChannel().close();
            channel.close();
            ChannelManager.getManager().removeChannel(channelContext);
            block.getAllocationProcessor().getRLogger().log("WORKER_SHUTDOWN blockid=" + block.getId());
        }
        catch (ChannelException e) {
            logger.warn(this + " failed to remove channel after shutdown");
        }        
    }

    public ChannelContext getChannelContext() {
        return channelContext;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return "Node [" + hostname + "] " + id;
    }

    public Collection<Cpu> getCpus() {
        return cpus;
    }

    public synchronized CoasterChannel getChannel() throws ChannelException {
        if (channel == null) {
            channel = ChannelManager.getManager().reserveChannel(channelContext);
            ChannelManager.getManager().reserveLongTerm(channel);
            channel.getChannelContext().addChannelListener(this);
        }
        return channel;
    }
    
    public void channelShutDown(Exception e) {
        if (logger.isInfoEnabled()) {
            logger.info(this + " lost connection to worker; removing node from block.", e);
        }
        for (Cpu cpu : cpus) {
            cpu.taskFailed("Connection to worker lost", e);
        }
        try {
            // the current breed of workers won't try to re-establish a connection, so
            // consider the channel lost for good (and lose the reference to it).
            // even if the workers do re-establish the connection, since the current
            // strategy is to fail the jobs they were running when the connection was lost,
            // treating them as entirely new workers should be just peachy 
            ChannelManager.getManager().removeChannel(channelContext);
        }
        catch (ChannelException ee) {
            logger.warn("Failed to remove channel", ee);
        }
        Settings settings = block.getAllocationProcessor().getSettings();
        Block block = getBlock();
        block.removeNode(this);
        Block.totalActiveWorkers -= settings.getJobsPerNode();
        Block.totalFailedWorkers += settings.getJobsPerNode();
        block.getAllocationProcessor().getRLogger().log("WORKER_LOST blockid=" + block.getId());
    }
}
