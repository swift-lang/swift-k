//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2008
 */
package org.globus.cog.karajan.scheduler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class OverloadedHostMonitor extends Thread {
	public static final Logger logger = Logger.getLogger(OverloadedHostMonitor.class);

	public static final int POLL_INTERVAL = 1000;
	private Set hosts;
	private WeightedHostScoreScheduler whss;

	private static final Integer[] DIRS = new Integer[] { new Integer(-1), new Integer(0),
			new Integer(1) };

	public OverloadedHostMonitor(WeightedHostScoreScheduler whss) {
		super("Overloaded Host Monitor");
		setDaemon(true);
		hosts = new HashSet();
		this.whss = whss;
		start();
	}

	public void add(WeightedHost wh) {
		synchronized (hosts) {
			if (!hosts.contains(wh)) {
				hosts.add(wh);
			}
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(POLL_INTERVAL);
				try {
					synchronized (hosts) {
						Iterator i = hosts.iterator();
						while (i.hasNext()) {
							WeightedHost wh = (WeightedHost) i.next();
							if (wh.isOverloaded() == 0) {
								whss.removeOverloaded(wh);
								i.remove();
							}
						}
					}
				}
				catch (Exception e) {
					logger.warn("Exception caught while polling hosts", e);
				}
			}
		}
		catch (InterruptedException e) {
			logger.info("Interrupted", e);
		}
	}
}
