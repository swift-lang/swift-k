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

public class HeartBeatCheckTask extends TimerTask {
	public static final Logger logger = Logger.getLogger(HeartBeatCheckTask.class);

	private CoasterChannel channel;
	private int multiplier, interval;

	public HeartBeatCheckTask(CoasterChannel channel, int interval, int multiplier) {
		this.channel = channel;
		this.multiplier = multiplier;
		this.interval = interval;
		channel.getChannelContext().setLastHeartBeat(System.currentTimeMillis());
	}

	public void run() {
		if (channel.isOffline()) {
			this.cancel();
		}
		else if (channel.isStarted()) {
			if (channel.getChannelContext().getLastHeartBeat() - interval * multiplier > System.currentTimeMillis()) {
				logger.warn("Channel (" + channel + ") has not received any heartbeat in "
						+ multiplier + " intervals. Shutting it down.");
				channel.shutdown();
			}
		}
	}
}
