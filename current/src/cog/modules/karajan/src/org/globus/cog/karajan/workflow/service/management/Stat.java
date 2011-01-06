//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 29, 2006
 */
package org.globus.cog.karajan.workflow.service.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Stat {
	private List users;

	public Stat() {
		users = new ArrayList();
	}

	public void addUser(User user) {
		users.add(user);
	}

	public Collection getUsers() {
		return users;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator i = users.iterator();
		while (i.hasNext()) {
			sb.append(i.next().toString());
		}
		return sb.toString();
	}

	public static class User {
		private List instances;
		private String name;

		public User(String name) {
			instances = new ArrayList();
			this.name = name;
		}

		public void addInstance(Instance instance) {
			instances.add(instance);
		}

		public Collection getInstances() {
			return instances;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(name);
			Iterator i = instances.iterator();
			while (i.hasNext()) {
				sb.append('\n');
				sb.append('\t');
				sb.append(i.next().toString());
			}
			return sb.toString();
		}
	}

	public static class Instance {
		private List runs;
		private String id, name;

		public Instance(String id, String name) {
			runs = new ArrayList();
			this.id = id;
			this.name = name;
		}

		public void addRun(Run run) {
			runs.add(run);
		}

		public Collection getRuns() {
			return runs;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(id);
			sb.append(" - ");
			sb.append(name);
			Iterator i = runs.iterator();
			while (i.hasNext()) {
				sb.append("\n\t\t");
				sb.append(i.next().toString());
			}
			return sb.toString();
		}
	}

	public static class Run {
		private String id, status, startTime, endTime;

		public Run(String id, String status, String startTime, String endTime) {
			this.id = id;
			this.status = status;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public String getId() {
			return id;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getStatus() {
			return status;
		}

		public String toString() {
			return id + ", status=" + status + ", startTime=" + startTime + ", endTime=" + endTime;
		}
	}
}
