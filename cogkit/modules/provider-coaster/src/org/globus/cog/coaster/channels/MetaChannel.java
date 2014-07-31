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
 * Created on Sep 18, 2005
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.RequestManager;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.handlers.RequestHandler;

public class MetaChannel extends AbstractCoasterChannel {
	private static final Logger logger = Logger.getLogger(MetaChannel.class);

	private CoasterChannel current;
	private TimerTask deactivator, poller;
	private boolean polling;
	private int tag;
	private boolean shuttingDown;
	
	public MetaChannel(ChannelContext channelContext) {
		super(null, channelContext, false);
	}

	public MetaChannel(RequestManager requestManager, ChannelContext channelContext) {
		super(requestManager, channelContext, false);
	}

	public synchronized void sendTaggedData(int tag, int flags, byte[] data, SendCallback cb) {
		current.sendTaggedData(tag, flags, data, cb);
	}
	
	@Override
    public void configureTimeoutChecks() {
        // only for the actual channels
    }

	public void registerCommand(Command cmd) throws ProtocolException {
		current.registerCommand(cmd);
	}

	public void registerHandler(RequestHandler handler, int tag) {
		current.registerHandler(handler, tag);
	}

	public void unregisterCommand(Command cmd) {
		current.unregisterCommand(cmd);
	}

	public void unregisterHandler(int tag) {
		current.unregisterHandler(tag);
	}

	public synchronized void bind(CoasterChannel channel) throws ChannelException {
		if (channel != null) {
			if (channel.getChannelContext() != this.getChannelContext() && channel.getChannelContext() != null) {
				throw new ChannelException("Trying to bind invalid channel (" + channel + ") to " + this);
			}
		}
		if (channel == current) {
			if (logger.isInfoEnabled()) {
				logger.info("Trying to re-bind current channel");
			}
			return;
		}
		if (logger.isInfoEnabled()) {
			logger.info(this + " binding to " + channel);
		}
		if (current instanceof Purgeable) {
			try {
				((Purgeable) current).purge(channel);
			}
			catch (IOException e) {
				throw new ChannelException("Could not purge channel", e);
			}
		}
		if (current != null) {
			current.shutdown();
		}
		current = channel;
		current.setRequestManager(getRequestManager());
	}
	
	public void close() {
		if (current != null) {
			current.close();
		}
	}

	public boolean isShuttingDown() {
		return shuttingDown;
	}

	public synchronized boolean isOffline() {
		return current == null || current.isOffline();
	}

	public synchronized void deactivateLater(int seconds) {
	    if (seconds == -1) {
	        return;
	    }
		if (deactivator != null) {
			deactivator.cancel();
		}
		deactivator = new TimerTask() {
			public void run() {
				ChannelManager.getManager().unregisterChannel(MetaChannel.this);
			}
		};
		Timer.schedule(deactivator, (long) seconds * 1000);
	}
	
	public synchronized void poll(final int seconds) {
		if (poller != null) {
			return;
		}
		poller = new TimerTask() {
			public void run() {
				if (getLongTermUsageCount() <= 0) {
					logger.info("Usage count: " + getLongTermUsageCount() + "; aborting poller.");
					this.cancel();
					poller = null;
					return;
				}
				if (!isOffline()) {
					return;
				}
				logger.info("Polling...");
				synchronized (MetaChannel.this) {
					if (polling) {
						return;
					}
					polling = true;
				}
				try {
					ChannelManager.getManager().reserveChannel(MetaChannel.this);
					ChannelManager.getManager().releaseChannel(MetaChannel.this);
				}
				catch (ChannelException e) {
					logger.warn("Exception caught while polling", e);
				}
				polling = false;
			}
		};
		long interval = (long) seconds * 1000;
		Timer.schedule(poller, interval, interval);
	}

	public String toString() {
		return "MetaChannel" + " [context: " + this.getChannelContext() + ", boundTo: " + current + "]";
	}

	public boolean isClient() {
		if (current != null) {
			return current.isClient();
		}
		return false;
	}

	public boolean isStarted() {
		CoasterChannel crt = current;
		if (crt != null) {
			return crt.isStarted();
		}
		else {
			return false;
		}
	}

	public void start() throws ChannelException {
	}
	
	public CoasterChannel current() {
		return current;
	}

	@Override
	public void setChannelContext(ChannelContext context) {
		super.setChannelContext(context);
	}
}
