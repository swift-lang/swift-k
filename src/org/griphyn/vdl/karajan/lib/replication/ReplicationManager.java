/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.griphyn.vdl.util.VDL2Config;

public class ReplicationManager {
	public static final Logger logger = Logger.getLogger(ReplicationManager.class);

	public static final int STATUS_NEEDS_REPLICATION = 100;
	
	public static final int INITIAL_QUEUE_TIME_ESTIMATE = 30; //seconds

	private int n;
	private long s;
	private long s2;
	private Map queued;
	private int minQueueTime, limit;
	private boolean enabled;
	private ReplicationGroups replicationGroups;
	private Scheduler scheduler;

	public ReplicationManager(Scheduler scheduler) {
		this.replicationGroups = new ReplicationGroups(scheduler);
		queued = new HashMap();
		try {
			minQueueTime = Integer.parseInt(VDL2Config.getConfig().getProperty(
					"replication.min.queue.time"));
			enabled = Boolean.valueOf(VDL2Config.getConfig().getProperty("replication.enabled")).booleanValue();
			limit = Integer.parseInt(VDL2Config.getConfig().getProperty("replication.limit"));
		}
		catch (Exception e) {
			logger.warn("Failed to get value of replication.min.queue.time property "
					+ "from Swift configuration. Using default (60s).", e);
			minQueueTime = 60;
		}
		if (enabled) {
			Sweeper.getSweeper().register(this);
		}
	}

	public void register(String rg, Task task) throws CanceledReplicaException {
		if (enabled) {
			replicationGroups.add(rg, task);
		}
	}

	public void submitted(Task task, Date time) {
		if (enabled) {
			synchronized (queued) {
				queued.put(task, time);
			}
		}
	}

	public void active(Task task, Date time) {
		if (enabled) {
			Date submitted;
			synchronized (queued) {
				submitted = (Date) queued.remove(task);
			}
			if (submitted != null) {
				long delta = (time.getTime() - submitted.getTime()) / 1000;
				synchronized (this) {
					n++;
					s += delta;
					s2 += delta * delta;
				}
			}
			replicationGroups.active(task);
		}
	}

	public synchronized int getN() {
		return n;
	}

	public synchronized double getMean() {
		if (n == 0) {
			return INITIAL_QUEUE_TIME_ESTIMATE; 
		}
		else {
			return s / n;
		}
	}

	public synchronized double getStandardDeviation() {
		if (n == 0) {
			return 0;
		}
		else {
			return Math.sqrt((s2 - s * s / n) / n);
		}
	}

	public void checkTasks() {
		Map m;
		synchronized (queued) {
			m = new HashMap(queued);
		}
		Iterator i = m.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			Task t = (Task) e.getKey();
			Date d = (Date) e.getValue();
			if (shouldBeReplicated(t, d)) {
				replicationGroups.requestReplica(t);
			}
		}
	}

	private boolean shouldBeReplicated(Task t, Date d) {
		if (t.getStatus().getStatusCode() == STATUS_NEEDS_REPLICATION) {
			// don't keep replicating the same job
			return false;
		}
		long inTheQueue = (System.currentTimeMillis() - d.getTime()) / 1000;
		if (inTheQueue > minQueueTime && inTheQueue > 3 * getMean()
				&& replicationGroups.getRequestedCount(t) < limit) {
			return true;
		}
		else {
			return false;
		}
	}
}