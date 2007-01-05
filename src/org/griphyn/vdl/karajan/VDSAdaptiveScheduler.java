/*
 * Created on Jun 12, 2006
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
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
import org.griphyn.common.catalog.TransformationCatalog;
import org.griphyn.common.catalog.transformation.File;
import org.griphyn.common.classes.TCType;
import org.griphyn.vdl.util.FQN;

public class VDSAdaptiveScheduler extends WeightedHostScoreScheduler {
	public static final Logger logger = Logger.getLogger(VDSAdaptiveScheduler.class);

	private static Timer timer;

	private TransformationCatalog tc;
	private Queue dq;
	private int clusteringQueueDelay = 1;
	private int minClusterTime = 60;
	private Map tasks;
	private boolean clusteringEnabled;

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
			tc = File.getNonSingletonInstance((String) value);
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
				dq.offer(new Object[] { task, constraints });
			}
		}
		else {
			super.enqueue(task, constraints);
		}
	}

	private synchronized Timer startTimer() {
		if (timer == null) {
			timer = new Timer("Clustering Timer", true);
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

	private void processDelayQueue() {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing clustering queue");
		}
		synchronized (dq) {
			while (!dq.isEmpty()) {
				int clusterTime = 0;
				Queue cluster = new LinkedList();
				Map env = new HashMap();
				Object constraints = null;
				String dir = null;

				Iterator dqi = dq.iterator();
				while (clusterTime < minClusterTime && dqi.hasNext()) {
					Object[] h = (Object[]) dqi.next();
					Task task = (Task) h[0];
					boolean envConflict = false;
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

					Iterator i = js.getEnvironmentVariableNames().iterator();
					while (i.hasNext()) {
						String envName = (String) i.next();
						Object value = env.get(envName);
						if (value != null && !value.equals(js.getEnvironmentVariable(envName))) {
							envConflict = true;
							break;
						}
					}

					if (envConflict) {
						continue;
					}
					else {
						dqi.remove();
					}

					i = js.getEnvironmentVariableNames().iterator();
					while (i.hasNext()) {
						String envName = (String) i.next();
						env.put(envName, js.getEnvironmentVariable(envName));
					}

					int maxWallTime = getMaxWallTime(task);
					clusterTime += maxWallTime;
					cluster.offer(h);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Got a cluster with size " + cluster.size());
				}

				if (cluster.size() == 0) {
					continue;
				}
				else if (cluster.size() == 1) {
					Object[] h = (Object[]) cluster.poll();
					super.enqueue((Task) h[0], h[1]);
				}
				else if (cluster.size() > 1) {
					Task t = new TaskImpl();
					t.setType(Task.JOB_SUBMISSION);
					t.setRequiredService(1);

					JobSpecification js = new JobSpecificationImpl();
					t.setSpecification(js);
					js.setExecutable("/bin/sh");
					js.addArgument("shared/seq.sh");
					js.setDirectory(dir);
					js.setAttribute("maxwalltime", String.valueOf(clusterTime));

					Iterator i = cluster.iterator();
					while (i.hasNext()) {
						Object[] h = (Object[]) i.next();
						Task st = (Task) h[0];
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

					synchronized (tasks) {
						tasks.put(t, cluster);
					}
					super.enqueue(t, new Contact[] { (Contact) constraints });
				}
			}
		}
	}

	private int getMaxWallTime(Task t) {
		return TypeUtil.toInt(((JobSpecification) t.getSpecification()).getAttribute("maxwalltime"));
	}

	protected void failTask(Task t, String message, Exception e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Failing task " + t.getIdentity());
		}
		Queue cluster = null;
		synchronized (tasks) {
			cluster = (Queue) tasks.get(t);
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
			Queue cluster = null;
			synchronized (tasks) {
				cluster = (Queue) tasks.get(t);
			}

			if (cluster == null) {
				super.statusChanged(e);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Got cluster status change for " + t.getIdentity());
				}
				Iterator i = cluster.iterator();
				while (i.hasNext()) {
					Object[] h = (Object[]) i.next();
					Task ct = (Task) h[0];
					StatusEvent nse = new StatusEvent(ct, e.getStatus());
					fireJobStatusChangeEvent(nse);
				}
				if (e.getStatus().isTerminal()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Removing cluster " + t.getIdentity());
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
		private TransformationCatalog tc;

		public TCChecker(TransformationCatalog tc) {
			this.tc = tc;
		}

		public boolean checkConstraints(BoundContact resource, TaskConstraints tc) {
			if (isPresent("trfqn", tc)) {
				FQN tr = (FQN) tc.getConstraint("trfqn");
				try {
					List l = this.tc.getTCEntries(tr.getNamespace(), tr.getName(), tr.getVersion(),
							resource.getHost(), TCType.INSTALLED);
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
