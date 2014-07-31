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
