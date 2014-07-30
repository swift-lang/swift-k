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
