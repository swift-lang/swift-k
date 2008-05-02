/*
 * Created on May 1, 2008
 */
package org.griphyn.vdl.karajan.lib.replication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

public class ReplicationGroups {
	private Map groups;
	private Map tasks;

	public ReplicationGroups() {
		groups = new HashMap();
		tasks = new HashMap();
	}

	public void add(String rg, Task task) {
		synchronized (groups) {
			List l = (List) groups.get(rg);
			if (l == null) {
				l = new LinkedList();
				groups.put(rg, l);
			}
			l.add(task);
			tasks.put(task.getIdentity(), rg);
		}
	}

	public void cancelReplicas(Task task) {
		List t = null;
		synchronized (groups) {
			String rg = (String) tasks.remove(task.getIdentity());
			if (rg != null) {
				t = (List) groups.remove(rg);
			}
			if (t != null) {
				Iterator i = t.iterator();
				while (i.hasNext()) {
					tasks.remove(i.next());
				}
			}
		}
		if (t != null) {
			Iterator i = t.iterator();
			while (i.hasNext()) {
				Task tr = (Task) i.next();
				if (tr != task) {
					tr.setStatus(new StatusImpl(Status.FAILED, "Replica cleanup", null));
				}
			}
		}
	}
}
