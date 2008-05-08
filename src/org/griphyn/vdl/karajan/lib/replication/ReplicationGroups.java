/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.scheduler.Scheduler;

public class ReplicationGroups {
	public static final Logger logger = Logger.getLogger(ReplicationGroups.class);
	private Map groups;
	private Map tasks;
	private Map schedulers;

	public ReplicationGroups() {
		groups = new HashMap();
		tasks = new HashMap();
		schedulers = new HashMap();
	}

	public void add(String rg, Task task, Scheduler scheduler) {
		synchronized (groups) {
			List l = (List) groups.get(rg);
			if (l == null) {
				l = new LinkedList();
				groups.put(rg, l);
			}
			l.add(task);
			tasks.put(task, rg);
			schedulers.put(task, scheduler);
		}
	}

	public void cancelReplicas(Task task) {
		if (logger.isDebugEnabled()) {
			logger.debug("Canceling replicas of " + task);
		}
		List t = null;
		synchronized (groups) {
			String rg = (String) tasks.remove(task);
			if (rg != null) {
				t = (List) groups.remove(rg);
			}
			if (t != null) {
				Iterator i = t.iterator();
				while (i.hasNext()) {
					Task tr = (Task) i.next();
					tasks.remove(tr);
				}
			}
		}
		if (t != null) {
			Iterator i = t.iterator();
			while (i.hasNext()) {
				Task tr = (Task) i.next();
				Scheduler s = (Scheduler) schedulers.remove(tr);
				if (tr != task) {
					if (logger.isDebugEnabled()) {
						logger.debug("Canceling " + tr);
					}
					s.cancelTask(tr);
				}
			}
		}
	}
	
	public int getGroupCount(Task task) {
		synchronized(groups) {
			String rg = (String) tasks.get(task);
			if (rg != null) {
				return ((List) groups.get(rg)).size();
			}
			else {
				return 0;
			}
		}
	}
}
