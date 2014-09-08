//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 6, 2014
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelListener;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;


public class Broadcaster implements ChannelListener, Callback {
    public static final Logger logger = Logger.getLogger(Broadcaster.class);
    
    private Set<CoasterChannel> channels;
    
    public Broadcaster() {
        channels = new HashSet<CoasterChannel>();
    }

    public void send(Command cmd) {
        List<CoasterChannel> l;
        synchronized(channels) {
            l = new ArrayList<CoasterChannel>(channels);
        }
        // allow send() to be called on the command, since
        // many of them build the data in that call
        Command crt = cmd;
        for (CoasterChannel channel : l) {
            if (channel.isClosed()) {
                logger.info("Channel is closed. Removing: " + channel);
                removeChannel(channel);
            }
            else {
                try {
                    crt.executeAsync(channel, this);
                }
                catch (ProtocolException e) {
                    logger.info("Could not send command " + cmd + " on channel " + channel, e);
                }
                crt = copyCommand(cmd);
            }
        }
    }

    private Command copyCommand(final Command cmd) {
        return new Command() {
            @Override
            public String getOutCmd() {
                return cmd.getOutCmd();
            }

            @Override
            public Collection<byte[]> getOutData() {
                return cmd.getOutData();
            }
        };
    }
    
    @Override
    public void replyReceived(Command cmd) {
        if (logger.isDebugEnabled()) {
            logger.debug("Reply received for " + cmd);
        }
    }

    @Override
    public void errorReceived(Command cmd, String msg, Exception t) {
        if (logger.isInfoEnabled()) {
            logger.info("Error received for " + cmd + ": " + msg, t);
        }
    }

    public void addChannel(CoasterChannel channel) {
        channel.addListener(this);
        synchronized(channels) {
            channels.add(channel);
        }
    }
    
    public void removeChannel(CoasterChannel channel) {
        synchronized(this) {
            channels.remove(channel);
        }
    }

    @Override
    public void channelClosed(CoasterChannel channel, Exception e) {
        if (logger.isInfoEnabled()) {
            logger.info("Removing channel from broadcast list: " + channel);
        }
        removeChannel(channel);
    }
}
