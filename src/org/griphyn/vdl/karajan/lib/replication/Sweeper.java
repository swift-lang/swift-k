/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.griphyn.vdl.karajan.lib.Execute;


public class Sweeper extends Thread {
	public static final int SWEEP_INTERVAL = 10000;
	private static Sweeper sweeper;

	public synchronized static Sweeper getSweeper() {
		if (sweeper == null) {
			sweeper = new Sweeper();
			sweeper.start();
		}
		return sweeper;
	}

	private List stats;

	public Sweeper() {
		setName("Queued Task Replication Sweeper");
		setDaemon(true);
		stats = new ArrayList();
	}

	public void register(ReplicationManager taskStatistics) {
		synchronized (stats) {
			stats.add(taskStatistics);
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(SWEEP_INTERVAL);
				List s;
				synchronized (stats) {
					s = new ArrayList(stats);
				}
				Iterator i = s.iterator();
				while (i.hasNext()) {
					try {
						((ReplicationManager) i.next()).checkTasks();
					}
					catch (Exception e) {
						Execute.logger.warn(e);
					}
				}
			}
		}
		catch (InterruptedException e) {
		}
	}
}