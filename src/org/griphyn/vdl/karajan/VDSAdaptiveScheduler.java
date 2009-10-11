/*
 * Created on Jun 12, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.scheduler.AbstractScheduler;
import org.globus.cog.karajan.scheduler.ResourceConstraintChecker;
import org.globus.cog.karajan.scheduler.TaskConstraints;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.util.FQN;

import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.swift.catalog.transformation.File;
import org.globus.swift.catalog.types.TCType;


public class VDSAdaptiveScheduler extends WeightedHostScoreScheduler {
	public static final Logger logger = Logger.getLogger(VDSAdaptiveScheduler.class);

	private static Timer timer;

	private TCCache tc;
	private LinkedList dq;
	private int clusteringQueueDelay = 1;
	private int minClusterTime = 60;
	private Map tasks;
	private boolean clusteringEnabled;
	private int clusterId;

	public VDSAdaptiveScheduler() {
		dq = new LinkedList();
		tasks = new HashMap();
	}

	public static final String PROP_TC_FILE = "transformationCatalogFile";
	public static final String PROP_CLUSTERING_ENABLED = "clusteringEnabled";
	public static final String PROP_CLUSTERING_QUEUE_DELAY = "clusteringQueueDelay";
	public static final String PROP_CLUSTERING_MIN_TIME = "clusteringMinTime";

	private static String[] propertyNames;

	public synchronized String[] getPropertyNames() {
		if (propertyNames == null) {
			propertyNames = AbstractScheduler.combineNames(super.getPropertyNames(),
					new String[] { PROP_TC_FILE });
		}
		return propertyNames;
	}

	public void setProperty(String name, Object value) {
		if (PROP_TC_FILE.equals(name)) {
			tc = new TCCache(File.getNonSingletonInstance((String) value));
			this.setConstraintChecker(new TCChecker(tc));
			this.addTaskTransformer(new VDSTaskTransformer(tc));
		}
		else if (PROP_CLUSTERING_QUEUE_DELAY.equals(name)) {
			clusteringQueueDelay = TypeUtil.toInt(value);
		}
		else if (PROP_CLUSTERING_MIN_TIME.equals(name)) {
			minClusterTime = TypeUtil.toInt(value);
		}
		else if (PROP_CLUSTERING_ENABLED.equals(name)) {
			clusteringEnabled = TypeUtil.toBoolean(value);
		}
		else {
			super.setProperty(name, value);
		}
	}

	public void enqueue(Task task, Object constraints) {
		if (shouldBeClustered(task, constraints)) {
			startTimer();
			if (logger.isDebugEnabled()) {
				logger.debug("Adding task to clustering queue: " + task.getIdentity());
			}
			synchronized (dq) {
				dq.addLast(new Object[] { task, constraints });
			}
		}
		else {
			super.enqueue(task, constraints);
		}
	}

	private synchronized Timer startTimer() {
		if (timer == null) {
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				public void run() {
					processDelayQueue();
				}
			}, clusteringQueueDelay * 1000, clusteringQueueDelay * 1000);
		}
		return timer;
	}

	private boolean shouldBeClustered(Task task, Object constraints) {
		if (!clusteringEnabled) {
			return false;
		}
		String reason = null;
		try {
			if (task.getType() != Task.JOB_SUBMISSION) {
				reason = "not a job";
				return false;
			}
			if (((JobSpecification) task.getSpecification()).getAttribute("maxwalltime") == null) {
				reason = "no maxwalltime";
				return false;
			}
			if (!(constraints instanceof Contact[])) {
				reason = "weird constraints";
				return false;
			}

			if (((Contact[]) constraints).length != 1) {
				reason = "constraints size != 1";
				return false;
			}
			boolean cluster = getMaxWallTime(task) < minClusterTime;
			if (!cluster) {
				reason = "not short enough";
			}
			return cluster;
		}
		finally {
			if (reason != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Task is not suitable for clustering (" + reason + ") "
							+ task.getIdentity());
				}
			}
		}
	}

	/*
	 * TODO Add maxmemory=max(maxmemory), minmemory=max(minmemory) and all other
	 * similar attributes
	 */
	private void processDelayQueue() {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing clustering queue");
		}
		synchronized (dq) {
			while (!dq.isEmpty()) {
				int clusterTime = 0;
				LinkedList cluster = new LinkedList();
				Map env = new HashMap();
				Map attrs = new HashMap();
				Object constraints = null;
				String dir = null;

				Iterator dqi = dq.iterator();
				while (clusterTime < minClusterTime && dqi.hasNext()) {
					Object[] h = (Object[]) dqi.next();
					Task task = (Task) h[0];

					JobSpecification js = (JobSpecification) task.getSpecification();

					if (constraints == null) {
						constraints = ((Object[]) h[1])[0];
					}
					else if (!constraints.equals(((Object[]) h[1])[0])) {
						continue;
					}

					if (dir == null) {
						dir = js.getDirectory() == null ? "" : js.getDirectory();
					}
					else if ((js.getDirectory() != null || !dir.equals(""))
							&& !dir.equals(js.getDirectory())) {
						continue;
					}

					if (detectConflict(js, env, attrs)) {
						continue;
					}
					else {
						dqi.remove();
					}

					merge(js, env, attrs);

					clusterTime += getMaxWallTime(task);
					cluster.addLast(h);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Got a cluster with size " + cluster.size());
				}

				if (cluster.size() == 0) {
					continue;
				}
				else if (cluster.size() == 1) {
					Object[] h = (Object[]) cluster.removeFirst();
					super.enqueue((Task) h[0], h[1]);
				}
				else if (cluster.size() > 1) {
					Task t = new TaskImpl();
					int thisClusterId = clusterId++;
					t.setIdentity(new IdentityImpl("cluster-" + thisClusterId));
					t.setType(Task.JOB_SUBMISSION);
					t.setRequiredService(1);

					JobSpecification js = new JobSpecificationImpl();
					t.setSpecification(js);
					js.setExecutable("/bin/sh");
					js.addArgument("shared/_swiftseq");
					js.addArgument("cluster-"+thisClusterId);
					js.addArgument("/clusters/"); // slice path more here TODO
					js.setDirectory(dir);
					js.setAttribute("maxwalltime", secondsToTime(clusterTime));
					
					if (logger.isInfoEnabled()) {
						logger.info("Creating cluster " + t.getIdentity() + " with size " + cluster.size());
					}

					Iterator i = cluster.iterator();
					while (i.hasNext()) {
						Object[] h = (Object[]) i.next();
						Task st = (Task) h[0];
						if (logger.isInfoEnabled()) {
							logger.info("Task " + st.getIdentity() + " clustered in " + t.getIdentity());
						}
						JobSpecification sjs = (JobSpecification) st.getSpecification();
						js.addArgument(sjs.getExecutable());
						List args = sjs.getArgumentsAsList();
						Iterator j = args.iterator();
						while (j.hasNext()) {
							String arg = (String) j.next();
							if (arg.equals("|")) {
								arg = "||";
							}
							js.addArgument(arg);
						}
						js.addArgument("|");
					}

					i = env.entrySet().iterator();
					while (i.hasNext()) {
						Map.Entry e = (Map.Entry) i.next();
						js.addEnvironmentVariable((String) e.getKey(), (String) e.getValue());
					}

					i = attrs.entrySet().iterator();
					while (i.hasNext()) {
						Map.Entry e = (Map.Entry) i.next();
						js.setAttribute((String) e.getKey(), (String) e.getValue());
					}

					synchronized (tasks) {
						tasks.put(t, cluster);
					}
					super.enqueue(t, new Contact[] { (Contact) constraints });
				}
			}
		}
	}

	private boolean detectConflict(JobSpecification js, Map env, Map attrs) {
		return detectEnvironmentConflict(js, env) || detectAttributeConflict(js, attrs);
	}

	private boolean detectEnvironmentConflict(JobSpecification js, Map env) {
		Iterator i = js.getEnvironmentVariableNames().iterator();
		while (i.hasNext()) {
			String envName = (String) i.next();
			Object value = env.get(envName);
			if (value != null && !value.equals(js.getEnvironmentVariable(envName))) {
				return true;
			}
		}
		return false;
	}

	private boolean detectAttributeConflict(JobSpecification js, Map attrs) {
		Iterator ia = js.getAttributeNames().iterator();
		while (ia.hasNext()) {
			String attrName = (String) ia.next();
			if (attrName.equals("maxwalltime")) {
				continue;
			}
			Object value = attrs.get(attrName);
			if (value != null && !value.equals(js.getAttribute(attrName))) {
				return true;
			}
		}
		return false;
	}

	private void merge(JobSpecification js, Map env, Map attrs) {
		mergeEnvironment(js, env);
		mergeAttributes(js, attrs);
	}

	private void mergeEnvironment(JobSpecification js, Map env) {
		Iterator i = js.getEnvironmentVariableNames().iterator();
		while (i.hasNext()) {
			String envName = (String) i.next();
			env.put(envName, js.getEnvironmentVariable(envName));
		}
	}

	private void mergeAttributes(JobSpecification js, Map attrs) {
		Iterator i = js.getAttributeNames().iterator();
		while (i.hasNext()) {
			String attrName = (String) i.next();
			if (attrName.equals("maxwalltime"))
				continue;
			attrs.put(attrName, js.getAttribute(attrName));
		}
	}

	private int getMaxWallTime(Task t) {
		return timeToSeconds(TypeUtil.toString(((JobSpecification) t.getSpecification()).getAttribute("maxwalltime")));
	}

	/**
	 * Valid times formats: Minutes, Hours:Minutes, Hours:Minutes:Seconds
	 */
	public static int timeToSeconds(String time) {
		String[] s = time.split(":");
		try {
			if (s.length == 1) {
				return 60 * Integer.parseInt(s[0]);
			}
			else if (s.length == 2) {
				return 60 * Integer.parseInt(s[1]) + 3600 * Integer.parseInt(s[0]);
			}
			else if (s.length == 3) {
				return Integer.parseInt(s[2]) + 60 * Integer.parseInt(s[1]) + 3600
						* Integer.parseInt(s[0]);
			}
		}
		catch (NumberFormatException e) {
		}
		throw new IllegalArgumentException("Invalid time specification: " + time);
	}

	public static String secondsToTime(int seconds) {
		StringBuffer sb = new StringBuffer();
		pad(sb, seconds / 3600);
		sb.append(':');
		pad(sb, (seconds % 3600) / 60);
		sb.append(':');
		pad(sb, seconds % 60);
		return sb.toString();
	}

	private static void pad(StringBuffer sb, int value) {
		if (value < 10) {
			sb.append('0');
		}
		sb.append(String.valueOf(value));
	}

	protected void failTask(Task t, String message, Exception e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Failing task " + t.getIdentity());
		}
		LinkedList cluster = null;
		synchronized (tasks) {
			cluster = (LinkedList) tasks.get(t);
		}
		if (cluster != null) {
			Iterator i = cluster.iterator();
			while (i.hasNext()) {
				Object[] h = (Object[]) i.next();
				super.failTask((Task) h[0], message, e);
			}
		}
		else {
			super.failTask(t, message, e);
		}

	}

	public void statusChanged(StatusEvent e) {
		Task t = (Task) e.getSource();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Got task status change for " + t.getIdentity());
			}
			LinkedList cluster = null;
			synchronized (tasks) {
				cluster = (LinkedList) tasks.get(t);
			}

			if (cluster == null) {
				super.statusChanged(e);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Got cluster status change for " + t.getIdentity());
				}

				Status clusterMemberStatus = e.getStatus();
				if(clusterMemberStatus.getStatusCode() == Status.FAILED) {
					clusterMemberStatus = new StatusImpl(Status.COMPLETED);
				}
				Iterator i = cluster.iterator();
				while (i.hasNext()) {
					Object[] h = (Object[]) i.next();
					Task ct = (Task) h[0];
					StatusEvent nse = new StatusEvent(ct, clusterMemberStatus);
					ct.setStatus(clusterMemberStatus);
					fireJobStatusChangeEvent(nse);
				}
				if (e.getStatus().isTerminal()) {
					if (logger.isInfoEnabled()) {
						logger.info("Removing cluster " + t.getIdentity());
					}
					synchronized (tasks) {
						tasks.remove(t);
					}
				}
			}
		}
		catch (Exception ex) {
			failTask(t, ex.getMessage(), ex);
		}
	}

	public static class TCChecker implements ResourceConstraintChecker {
		private TCCache tc;

		public TCChecker(TCCache tc) {
			this.tc = tc;
		}

		public boolean checkConstraints(BoundContact resource, TaskConstraints tc) {
			if (isPresent("trfqn", tc)) {
				FQN tr = (FQN) tc.getConstraint("trfqn");
				try {
					List l = this.tc.getTCEntries(tr, resource.getHost(), TCType.INSTALLED);
					if (l == null || l.isEmpty()) {
						return false;
					}
					else {
						return true;
					}
				}
				catch (Exception e) {
					logger.warn("Exception caught while querying TC", e);
					return false;
				}
			}
			else {
				return true;
			}
		}

		private boolean isPresent(String constraint, TaskConstraints t) {
			if (t == null) {
				return false;
			}
			if (t.getConstraint(constraint) == null) {
				return false;
			}
			return true;
		}

		public List checkConstraints(List resources, TaskConstraints tc) {
			LinkedList l = new LinkedList();
			Iterator i = resources.iterator();
			while (i.hasNext()) {
				BoundContact res = (BoundContact) i.next();
				if (checkConstraints(res, tc)) {
					l.add(res);
				}
			}
			return l;
		}
	}
}
