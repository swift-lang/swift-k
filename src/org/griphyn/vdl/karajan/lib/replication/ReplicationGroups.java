/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.scheduler.Scheduler;

public class ReplicationGroups {

	public static final Logger logger = Logger.getLogger(ReplicationGroups.class);
	private Map groups;
	private Map tasks;
	private Scheduler scheduler;

	public ReplicationGroups(Scheduler scheduler) {
		groups = new HashMap();
		tasks = new HashMap();
		this.scheduler = scheduler;
	}

	public void add(String rg, Task task) throws CanceledReplicaException {
		synchronized (groups) {
			tasks.put(task, rg);
			getGroup(rg).addTask(task);
		}
	}

	private Group getGroup(String id) {
		synchronized (groups) {
			Group group = (Group) groups.get(id);
			if (group == null) {
				group = new Group();
				groups.put(id, group);
			}
			return group;
		}
	}

	private Group getGroup(Task t) {
		synchronized (groups) {
			return getGroup((String) tasks.get(t));
		}
	}

	private Group removeGroup(String id) {
		return (Group) groups.remove(id);
	}

	public void active(Task task) {
		getGroup(task).active(task);
	}

	protected void removeGroup(Task t) {
		synchronized (groups) {
			String rg = (String) tasks.remove(t);
			if (logger.isDebugEnabled()) {
				logger.debug("Remove group " + rg);
			}
			Group g = removeGroup(rg);
			tasks.keySet().removeAll(g.getAllTasks());
		}
	}

	public int getRequestedCount(Task task) {
		return getGroup(task).getRequestedCount();
	}

	public void requestReplica(Task t) {
		getGroup(t).replicaRequested();
		t.setStatus(new StatusImpl(ReplicationManager.STATUS_NEEDS_REPLICATION));
	}

	private class Group {
		// requested but not necessarily submitted
		private int requested, pending;
		private boolean success;
		private List l;

		public Group() {
			requested = 1;
			pending = 1;
			l = new LinkedList();
		}

		public synchronized int getRequestedCount() {
			return requested;
		}

		public synchronized void replicaRequested() {
			requested++;
			pending++;
		}

		public void active(Task task) {
			synchronized (this) {
				success = true;
				checkPending(task);
			}
			cancelAllBut(task);
		}

		public Collection getAllTasks() {
			return l;
		}

		private void checkPending(Task t) {
			if (pending == 0) {
				// all done				
				removeGroup(t);
			}
		}

		public synchronized void addTask(Task t) throws CanceledReplicaException {
			pending--;
			if (success) {
				checkPending(t);
				throw new CanceledReplicaException();
			}
			else {
				l.add(t);
			}
		}

		public void cancelAllBut(Task task) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cancel all but " + task);
			}
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Task t = (Task) i.next();
				if (t != task) {
					cancelTask(t);
				}
			}
		}

		private void cancelTask(Task t) {
			if (logger.isDebugEnabled()) {
				logger.debug("Canceling " + t);
			}
			scheduler.cancelTask(t);
		}
	}
}
