//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2008
 */
package org.globus.cog.karajan.scheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class OverloadedHostMonitor extends Thread {
	public static final Logger logger = Logger.getLogger(OverloadedHostMonitor.class);

	public static final int POLL_INTERVAL = 1000;
	private Map hosts;
	private WeightedHostScoreScheduler whss;

	private static final Integer[] DIRS = new Integer[] { new Integer(-1), new Integer(0),
			new Integer(1) };

	public OverloadedHostMonitor(WeightedHostScoreScheduler whss) {
		super("Overloaded Host Monitor");
		setDaemon(true);
		hosts = new HashMap();
		this.whss = whss;
		start();
	}

	public void add(WeightedHost wh, int dir) {
		synchronized (hosts) {
			hosts.put(wh, DIRS[dir + 1]);
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(POLL_INTERVAL);
				try {
					synchronized (hosts) {
						Iterator i = hosts.entrySet().iterator();
						while (i.hasNext()) {
							Map.Entry e = (Map.Entry) i.next();
							WeightedHost wh = (WeightedHost) e.getKey();
							if (wh.isOverloaded() == 0) {
								Integer dir = (Integer) e.getValue();
								whss.updateOverloadedCount(dir.intValue());
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
