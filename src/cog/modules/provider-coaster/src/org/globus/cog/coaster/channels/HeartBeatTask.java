//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 9, 2008
 */
package org.globus.cog.coaster.channels;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.HeartBeatCommand;
import org.globus.cog.coaster.commands.Command.Callback;

public class HeartBeatTask extends TimerTask implements Callback {
	public static final Logger logger = Logger.getLogger(HeartBeatTask.class);

	private CoasterChannel channel;

	public HeartBeatTask(CoasterChannel channel) {
		this.channel = channel;
	}

	public void run() {
		if (channel.isOffline()) {
			this.cancel();
		}
		else if (channel.isStarted()) {
			HeartBeatCommand hbc = new HeartBeatCommand();
			try {
				hbc.executeAsync(channel, this);
			}
			catch (ProtocolException e) {
				this.cancel();
				logger.error("Protocol error caught while trying to send heartbeat. " +
						"Suspending heartbeats for this channel.", e);
			}
		}
	}

	public void errorReceived(Command cmd, String msg, Exception t) {
		// these can't fail in normal operation
		// so this is a channel error, which should be handled
		// by the channel (but we do get notifications for it)
		logger.info("Heartbeat failed: " + msg, t);
	}

	public void replyReceived(Command cmd) {
		// we're good
	}
}
