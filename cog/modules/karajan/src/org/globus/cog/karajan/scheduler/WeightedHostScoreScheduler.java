//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 20, 2005
 */
package org.globus.cog.karajan.scheduler;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;

public class WeightedHostScoreScheduler extends LateBindingScheduler {
	private static final Logger logger = Logger.getLogger(WeightedHostScoreScheduler.class);

	public static final int POLICY_WEIGHTED_RANDOM = 0;
	public static final int POLICY_BEST_SCORE = 1;
	public static final String FACTOR_CONNECTION_REFUSED = "connectionRefusedFactor";
	public static final String FACTOR_CONNECTION_TIMEOUT = "connectionTimeoutFactor";
	public static final String FACTOR_SUBMISSION_TASK_LOAD = "jobSubmissionTaskLoadFactor";
	public static final String FACTOR_TRANSFER_TASK_LOAD = "transferTaskLoadFactor";
	public static final String FACTOR_FILEOP_TASK_LOAD = "fileOperationTaskLoadFactor";
	public static final String FACTOR_SUCCESS = "successFactor";
	public static final String FACTOR_FAILURE = "failureFactor";
	public static final String SCORE_HIGH_CAP = "scoreHighCap";
	public static final String POLICY = "policy";

	private WeightedHostSet sorted;
	private int policy;

	/*
	 * These field names must match the property names
	 */
	private double connectionRefusedFactor, connectionTimeoutFactor, jobSubmissionTaskLoadFactor,
			transferTaskLoadFactor, fileOperationTaskLoadFactor, successFactor, failureFactor,
			scoreHighCap;

	private int jobThrottle;

	public WeightedHostScoreScheduler() {
		policy = POLICY_WEIGHTED_RANDOM;
		setDefaultFactors();
	}

	protected final void setDefaultFactors() {
		connectionRefusedFactor = -10;
		connectionTimeoutFactor = -20;
		jobSubmissionTaskLoadFactor = -0.2;
		transferTaskLoadFactor = -0.2;
		fileOperationTaskLoadFactor = -0.01;
		successFactor = 1;
		failureFactor = -0.1;
		scoreHighCap = 100;
		jobThrottle = 2;
	}

	public void setResources(ContactSet grid) {
		super.setResources(grid);
		sorted = new WeightedHostSet(scoreHighCap);
		Iterator i = grid.getContacts().iterator();
		while (i.hasNext()) {
			addToSorted(new WeightedHost((BoundContact) i.next()));
		}
	}

	protected void addToSorted(WeightedHost wh) {
		sorted.add(wh);
	}

	protected synchronized void multiplyScore(WeightedHost wh, double factor) {
		double score = wh.getScore();
		if (logger.isDebugEnabled()) {
			logger.debug("multiplyScore(" + wh + ", " + factor + ")");
		}
		double ns = factor(score, factor);
		sorted.changeScore(wh, ns);
		if (logger.isDebugEnabled()) {
			logger.debug("Old score: " + score + ", new score: " + ns);
		}
	}

	protected synchronized void multiplyScoreLater(WeightedHost wh, double factor) {
		wh.setDelayedDelta(wh.getDelayedDelta() + factor);
	}

	protected double factor(double score, double factor) {
		return score + factor;
	}

	protected synchronized BoundContact getNextContact(TaskConstraints t)
			throws NoFreeResourceException {
		checkGlobalLoadConditions();
		BoundContact contact;

		WeightedHostSet s = sorted;
		WeightedHost selected = null;

		s = constrain(s, getConstraintChecker(), t);

		double sum = s.getSum();
		if (policy == POLICY_WEIGHTED_RANDOM) {
			double rand = Math.random() * sum;
			if (logger.isInfoEnabled() && !s.isEmpty()) {
				logger.info("Sorted: " + s);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Rand: " + rand + ", sum: " + sum);
			}
			Iterator i = s.iterator();

			sum = 0;
			while (i.hasNext()) {
				WeightedHost wh = (WeightedHost) i.next();
				sum += wh.getTScore();
				if (sum >= rand) {
					selected = wh;
					break;
				}
			}
			if (selected == null) {
				if (s.isEmpty()) {
					throw new NoFreeResourceException();
				}
				else {
					selected = s.last();
				}
			}
		}
		else if (policy == POLICY_BEST_SCORE) {
			selected = s.last();
		}
		else {
			throw new KarajanRuntimeException("Invalid policy number: " + policy);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Next contact: " + selected.getHost());
		}
		selected.changeLoad(1);
		selected.setDelayedDelta(successFactor);
		return selected.getHost();
	}

	public void releaseContact(BoundContact contact) {
		if (logger.isDebugEnabled()) {
			logger.debug("Releasing contact " + contact);
		}
		super.releaseContact(contact);
		WeightedHost wh = sorted.findHost(contact);
		if (wh != null) {
			wh.changeLoad(-1);
			sorted.changeScore(wh, wh.getScore() + wh.getDelayedDelta());
		}
		else {
			logger.warn("ghost contact (" + contact + ") in releaseContact");
		}
	}

	protected WeightedHostSet constrain(WeightedHostSet s, ResourceConstraintChecker rcc,
			TaskConstraints tc) {
		if (rcc == null) {
			return s;
		}
		else {
			WeightedHostSet ns = new WeightedHostSet(scoreHighCap);
			Iterator i = s.iterator();
			while (i.hasNext()) {
				WeightedHost wh = (WeightedHost) i.next();
				if (rcc.checkConstraints(wh.getHost(), tc) && notOverloaded(wh)) {
					ns.add(wh);
				}
			}
			return ns;
		}
	}

	protected boolean notOverloaded(WeightedHost wh) {
		double score = wh.getTScore();
		int load = wh.getLoad();
		return load < jobThrottle * score + 2;
	}

	private static String[] propertyNames;
	private static final String[] myPropertyNames = new String[] { POLICY,
			FACTOR_CONNECTION_REFUSED, FACTOR_CONNECTION_TIMEOUT, FACTOR_SUBMISSION_TASK_LOAD,
			FACTOR_TRANSFER_TASK_LOAD, FACTOR_FILEOP_TASK_LOAD, FACTOR_FAILURE, FACTOR_SUCCESS,
			SCORE_HIGH_CAP };
	private static Set propertyNamesSet;

	static {
		propertyNamesSet = new HashSet();
		for (int i = 0; i < myPropertyNames.length; i++) {
			propertyNamesSet.add(myPropertyNames[i]);
		}
	}

	public synchronized String[] getPropertyNames() {
		if (propertyNames == null) {
			propertyNames = AbstractScheduler.combineNames(super.getPropertyNames(),
					myPropertyNames);
		}
		return propertyNames;
	}

	public void setProperty(String name, Object value) {
		if (propertyNamesSet.contains(name)) {
			if (POLICY.equals(name)) {
				if (value instanceof String) {
					value = ((String) value).toLowerCase();
				}
				if ("random".equals(value)) {
					policy = POLICY_WEIGHTED_RANDOM;
				}
				else if ("best".equals("value")) {
					policy = POLICY_BEST_SCORE;
				}
				else {
					throw new KarajanRuntimeException("Unknown policy type: " + value);
				}
			}
			else {
				double val = TypeUtil.toDouble(value);
				try {
					Field f = this.getClass().getField(name);
					if (f.getClass().equals(int.class)) {
						f.setInt(this, (int) val);
					}
					else {
						f.setDouble(this, val);
					}
				}
				catch (Exception e) {
					throw new KarajanRuntimeException("Failed to set property '" + name + "'", e);
				}
			}
		}
		else {
			super.setProperty(name, value);
		}
	}

	public void submitBoundToServices(Task t, Contact[] contacts, Service[] services)
			throws TaskSubmissionException {
		factorSubmission(t, contacts, 1);
		super.submitBoundToServices(t, contacts, services);
	}

	public void statusChanged(StatusEvent e) {
		try {
			Task t = (Task) e.getSource();
			int code = e.getStatus().getStatusCode();
			Contact[] contacts = getContacts(t);
			if (code == Status.SUBMITTED) {
				// this isn't reliable
				// factorSubmission(t, contacts, 1);
			}
			else if (code == Status.COMPLETED) {
				factorSubmission(t, contacts, -1);
				factorMultipleLater(contacts, successFactor);
			}
			else if (code == Status.FAILED) {
				factorMultipleLater(contacts, failureFactor);
				Exception ex = e.getStatus().getException();
				if (ex != null) {
					String exs = ex.toString();
					if (exs.indexOf("Connection refused") >= 0
							|| exs.indexOf("connection refused") >= 0) {
						factorMultipleLater(contacts, connectionRefusedFactor);
					}
					else if (exs.indexOf("timeout") >= 0) {
						factorMultipleLater(contacts, connectionTimeoutFactor);
					}
				}
			}
		}
		catch (Exception ex) {
			logger.warn("Scheduler threw exception while processing task status change", ex);
		}
		finally {
			super.statusChanged(e);
		}
	}

	private void factorSubmission(Task t, Contact[] contacts, int exp) {
		// I wonder where the line between abstraction and obfuscation is...
		if (t.getType() == Task.JOB_SUBMISSION) {
			factorMultiple(contacts, spow(jobSubmissionTaskLoadFactor, exp));
		}
		else if (t.getType() == Task.FILE_TRANSFER) {
			factorMultiple(contacts, spow(transferTaskLoadFactor, exp));
		}
		else if (t.getType() == Task.FILE_OPERATION) {
			factorMultiple(contacts, spow(fileOperationTaskLoadFactor, exp));
		}
	}

	private double spow(double x, int exp) {
		if (exp == 1) {
			return x;
		}
		else if (exp == -1) {
			return -x;
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	private void factorMultiple(Contact[] contacts, double factor) {
		for (int i = 0; i < contacts.length; i++) {
			BoundContact bc = (BoundContact) contacts[i];
			WeightedHost wh = sorted.findHost(bc);
			if (wh != null) {
				multiplyScore(wh, factor);
			}
		}
	}

	private void factorMultipleLater(Contact[] contacts, double factor) {
		for (int i = 0; i < contacts.length; i++) {
			BoundContact bc = (BoundContact) contacts[i];
			WeightedHost wh = sorted.findHost(bc);
			if (wh != null) {
				multiplyScoreLater(wh, factor);
			}
		}
	}
}
