//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2012
 */
package org.globus.cog.coaster.channels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

class Multiplexer extends Thread {
	public static final Logger logger = Logger.getLogger(Multiplexer.class);

	private Set<CoasterChannel> channels;
	private List<CoasterChannel> remove, add;
	private boolean terminated;
	private int id;

	public Multiplexer(int id) {
		super("Channel multiplexer " + id);
		this.id = id;
		setDaemon(true);
		channels = new HashSet<CoasterChannel>();
		remove = Collections.synchronizedList(new ArrayList<CoasterChannel>());
		add = Collections.synchronizedList(new ArrayList<CoasterChannel>());
	}

	public synchronized void register(AbstractStreamCoasterChannel channel) {
		add.add(channel);
		if (logger.isInfoEnabled()) {
			logger.info("(" + id + ") Scheduling " + channel + " for addition");
		}
		if (terminated) {
			logger.warn("Trying to add a channel to a stopped multiplexer");
		}
	}

	public void run() {
		logger.info("Multiplexer " + id + " started");
		boolean any;
		long last = System.currentTimeMillis();
		try {
			while (true) {
				any = false;
				if (this == AbstractStreamCoasterChannel.multiplexer[0]) {
					AbstractStreamCoasterChannel.savail = 0;
					AbstractStreamCoasterChannel.cnt = 0;
				}
				Iterator<CoasterChannel> i = channels.iterator();
				while (i.hasNext()) {
					AbstractStreamCoasterChannel channel = 
						(AbstractStreamCoasterChannel) i.next();
					if (channel.isClosed()) {
						if (logger.isInfoEnabled()) {
							logger.info("Channel is closed. Removing.");
						}
						i.remove();
					}
					try {
						any |= channel.step();
					}
					catch (Exception e) {
					    logger.info("Exception in channel step", e);
						try {
							shutdown(channel, e);
						}
						catch (Exception ee) {
							logger.warn("Failed to shut down channel", ee);
						}
					}
				}
				synchronized (this) {
					i = remove.iterator();
					while (i.hasNext()) {
						Object r = i.next();
						channels.remove(r);
					}
					i = add.iterator();
					while (i.hasNext()) {
						CoasterChannel a = i.next();
						channels.add(a);
					}
					remove.clear();
					add.clear();
				}
				if (logger.isDebugEnabled()) {
    				if (this == AbstractStreamCoasterChannel.multiplexer[0]) {
    					long now = System.currentTimeMillis();
    					if (now - last > 10000) {
    						if (AbstractStreamCoasterChannel.cnt > 0) {
    							logger.debug("Avg stream buf: " + (AbstractStreamCoasterChannel.savail / AbstractStreamCoasterChannel.cnt));
    						}
    						else {
    							logger.debug("No streams");
    						}
    						last = now;
    					}
    				}
				}
				if (!any) {
					Thread.sleep(20);
				}
			}
		}
		catch (Exception e) {
			logger.warn("Exception in channel multiplexer", e);
		}
		catch (Error e) {
			logger.error("Error in multiplexer", e);
			e.printStackTrace();
			System.exit(10);
		}
		finally {
			logger.info("Multiplexer finished");
			terminated = true;
		}
	}

	public synchronized void unregister(AbstractStreamCoasterChannel channel) {
		if (logger.isInfoEnabled()) {
			logger.info("Unregistering channel " + channel);
		}
		remove.add(channel);
	}

	private void shutdown(AbstractStreamCoasterChannel channel, Exception e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Channel exception caught", e);
		}
		channel.handleChannelException(e);
		synchronized (this) {
			if (logger.isInfoEnabled()) {
				logger.info("Removing faulty channel " + channel);
			}
			remove.add(channel);
		}
	}
}