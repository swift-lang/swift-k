/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Task;
import org.griphyn.vdl.util.VDL2Config;

public class ReplicationManager {
	public static final Logger logger = Logger.getLogger(ReplicationManager.class);
	
	// maybe a silly question, but how does one safely extends enums?
	public static final int STATUS_NEEDS_REPLICATION = 100;

	private int n;
	private long s;
	private long s2;
	private Map queued;
	private int minQueueTime;
	private boolean enabled;

	public ReplicationManager() {
		queued = new HashMap();
		try {
			minQueueTime = Integer.parseInt(VDL2Config.getConfig().getProperty(
					"replication.min.queue.time"));
			enabled = Boolean.valueOf(VDL2Config.getConfig().getProperty(
					"replication.enabled")).booleanValue();
		}
		catch (Exception e) {
			logger.warn("Failed to get value of replication.min.queue.time property " +
					"from Swift configuration. Using default (60s).", e);
			minQueueTime = 60;
		}
		if (enabled) {
			Sweeper.getSweeper().register(this);
		}
	}

	public void submitted(Task task, Date time) {
		if (!enabled) {
			return;
		}
		synchronized (queued) {
			queued.put(task.getIdentity(), time);
		}
	}

	public void active(Task task, Date time) {
		if (!enabled) {
			return;
		}
		Date submitted;
		synchronized (queued) {
			submitted = (Date) queued.remove(task.getIdentity());
		}
		if (submitted != null) {
			long delta = (time.getTime() - submitted.getTime()) / 1000;
			synchronized (this) {
				n++;
				s += delta;
				s2 += delta * delta;
			}
		}
	}

	public synchronized int getN() {
		return n;
	}

	public synchronized double getMean() {
		return s / n;
	}

	public synchronized double getStandardDeviation() {
		return Math.sqrt((s2 - s * s / n) / n);
	}

	public void checkTasks() {
		if (n == 0) {
			return;
		}
		Map m;
		synchronized (queued) {
			m = new HashMap(queued);
		}
		Iterator i = m.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			Task t = (Task) e.getKey();
			Date d = (Date) e.getValue();
			if (shouldBeReplicated(d)) {
				t.setStatus(new StatusImpl(STATUS_NEEDS_REPLICATION));
			}
		}
	}

	private boolean shouldBeReplicated(Date d) {
		long inTheQueue = (System.currentTimeMillis() - d.getTime()) / 1000;
		if (n > 0 && inTheQueue > minQueueTime && inTheQueue > 3 * getMean()) {
			return true;
		}
		else {
			return false;
		}
	}
}