//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 7, 2012
 */
package org.globus.cog.karajan.workflow.service.channels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

class Multiplexer extends Thread {
	public static final Logger logger = Logger.getLogger(Multiplexer.class);

	private Set<KarajanChannel> channels;
	private List<KarajanChannel> remove, add;
	private boolean terminated;
	private int id;

	public Multiplexer(int id) {
		super("Channel multiplexer " + id);
		this.id = id;
		setDaemon(true);
		channels = new HashSet<KarajanChannel>();
		remove = Collections.synchronizedList(new ArrayList<KarajanChannel>());
		add = Collections.synchronizedList(new ArrayList<KarajanChannel>());
	}

	public synchronized void register(AbstractStreamKarajanChannel channel) {
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
				if (this == AbstractStreamKarajanChannel.multiplexer[0]) {
					AbstractStreamKarajanChannel.savail = 0;
					AbstractStreamKarajanChannel.cnt = 0;
				}
				Iterator<KarajanChannel> i = channels.iterator();
				while (i.hasNext()) {
					AbstractStreamKarajanChannel channel = 
						(AbstractStreamKarajanChannel) i.next();
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
						KarajanChannel a = i.next();
						channels.add(a);
					}
					remove.clear();
					add.clear();
				}
				if (this == AbstractStreamKarajanChannel.multiplexer[0]) {
					long now = System.currentTimeMillis();
					if (now - last > 10000) {
						if (AbstractStreamKarajanChannel.cnt > 0) {
							logger.info("Avg stream buf: " + (AbstractStreamKarajanChannel.savail / AbstractStreamKarajanChannel.cnt));
						}
						else {
							logger.info("No streams");
						}
						last = now;
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

	public synchronized void unregister(AbstractStreamKarajanChannel channel) {
		if (logger.isInfoEnabled()) {
			logger.info("Unregistering channel " + channel);
		}
		remove.add(channel);
	}

	private void shutdown(AbstractStreamKarajanChannel channel, Exception e) {
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