//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 18, 2005
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.RequestManager;
import org.globus.cog.karajan.workflow.service.UserContext;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class MetaChannel extends AbstractKarajanChannel {
	private static final Logger logger = Logger.getLogger(MetaChannel.class);

	private KarajanChannel current;
	private TimerTask deactivator, poller;
	private boolean polling;
	private int tag;
	private boolean shuttingDown;

	public MetaChannel(ChannelContext channelContext) {
		super(null, channelContext);
	}

	public MetaChannel(RequestManager requestManager, ChannelContext channelContext) {
		super(requestManager, channelContext);
	}

	public synchronized void sendTaggedData(int tag, int flags, byte[] data) {
		current.sendTaggedData(tag, flags, data);
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

	public UserContext getUserContext() {
		return current.getUserContext();
	}

	public synchronized void bind(KarajanChannel channel) throws ChannelException {
		if (channel == current) {
			logger.warn("Trying to re-bind current channel");
			return;
		}
		logger.info(this + ".bind -> " + channel);
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

	public boolean isShuttingDown() {
		return shuttingDown;
	}

	public synchronized boolean isOffline() {
		return current == null || current.isOffline();
	}

	public synchronized void deactivateLater(int seconds) {
		if (deactivator != null) {
			deactivator.cancel();
		}
		deactivator = new TimerTask() {
			public void run() {
				try {
					ChannelManager.getManager().unregisterChannel(MetaChannel.this);
				}
				catch (ChannelException e) {
					logger.warn("Exception caught while unregistering channel", e);
				}
			}
		};
		getTimer().schedule(deactivator, (long) seconds * 1000);
	}
	
	public Timer getTimer() {
		return getChannelContext().getTimer();
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
		getTimer().schedule(poller, interval, interval);
	}

	public String toString() {
		return "MetaChannel: " + System.identityHashCode(this) + " -> " + current;
	}

	public boolean isClient() {
		if (current != null) {
			return current.isClient();
		}
		return false;
	}

	public void start() throws ChannelException {
	}
}
