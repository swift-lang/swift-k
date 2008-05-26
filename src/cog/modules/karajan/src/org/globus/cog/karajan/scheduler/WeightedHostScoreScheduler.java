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
import java.util.Date;
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
	public static final String INITIAL_SCORE = "initialScore";
	public static final String SCORE_HIGH_CAP = "scoreHighCap";
	public static final String POLICY = "policy";
	public static final String JOB_THROTTLE = "jobThrottle";
	public static final String MAX_SUBMISSION_TIME = "maxSubmissionTime";

	private WeightedHostSet sorted;
	private int policy;

	/*
	 * These field names must match the property names
	 */
	private double connectionRefusedFactor, connectionTimeoutFactor, jobSubmissionTaskLoadFactor,
			transferTaskLoadFactor, fileOperationTaskLoadFactor, successFactor, failureFactor,
			scoreHighCap, maxSubmissionTime;

	private float defaultJobThrottle;

	private double submissionTimeBias, submissionTimeFactor;

	private boolean change;
	private TaskConstraints cachedConstraints;
	private boolean cachedLoadState;
	private int hits;

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
		successFactor = 0.1;
		failureFactor = -0.5;
		scoreHighCap = 100;
		defaultJobThrottle = 2;
		maxSubmissionTime = 20;
		updateInternal();
	}

	// This isn't very accurate since it varies by provider
	// and with submission parallelism
	// What it means is that if submission time is exactly this much
	// then a success should increase the score by successFactor
	public final double BASE_SUBMISSION_TIME = 0.5;

	protected void updateInternal() {
		if (maxSubmissionTime < BASE_SUBMISSION_TIME) {
			throw new IllegalArgumentException("maxSubmissionTime must be > " + BASE_SUBMISSION_TIME);
		}
		submissionTimeFactor = -successFactor / (maxSubmissionTime - BASE_SUBMISSION_TIME);
		submissionTimeBias = -BASE_SUBMISSION_TIME * submissionTimeFactor;
	}

	public void setResources(ContactSet grid) {
		super.setResources(grid);
		if (grid.getContacts() == null) {
			return;
		}
		sorted = new WeightedHostSet(scoreHighCap);
		Iterator i = grid.getContacts().iterator();
		while (i.hasNext()) {
			BoundContact contact = (BoundContact) i.next();
			float thisJobThrottle = defaultJobThrottle;
			// TODO constants instead of these literals
			if(contact.hasProperty(JOB_THROTTLE)) {
				thisJobThrottle = floatThrottleValue(contact.getProperty(JOB_THROTTLE));
			}
			WeightedHost wh;
			if(contact.hasProperty(INITIAL_SCORE)) {
				double thisInitialScore = Double.parseDouble((String)contact.getProperty(INITIAL_SCORE));
				wh = new WeightedHost(contact, thisInitialScore, thisJobThrottle);
			} else {
				wh = new WeightedHost(contact, thisJobThrottle);
			}
			addToSorted(wh);
		}
	}

	protected void addToSorted(WeightedHost wh) {
		sorted.add(wh);
	}

	protected synchronized void factorScore(WeightedHost wh, double factor) {
		double score = wh.getScore();
		if (logger.isDebugEnabled()) {
			logger.debug("multiplyScore(" + wh + ", " + factor + ")");
		}
		double ns = factor(score, factor);
		sorted.changeScore(wh, ns);
		if (logger.isDebugEnabled()) {
			logger.debug("Old score: " + WeightedHost.D4.format(score) + ", new score: "
					+ WeightedHost.D4.format(ns));
		}
	}

	protected synchronized void factorScoreLater(WeightedHost wh, double factor) {
		wh.setDelayedDelta(wh.getDelayedDelta() + factor);
	}

	protected final double factor(double score, double factor) {
		return score + factor;
	}

	protected synchronized BoundContact getNextContact(TaskConstraints t)
			throws NoFreeResourceException {
		checkGlobalLoadConditions();

		if (!change && cachedLoadState && cachedConstraints.equals(t)) {
			hits++;
			throw new NoFreeResourceException();
		}

		BoundContact contact;

		WeightedHostSet s = sorted;
		WeightedHost selected = null;

		if (s.allOverloaded()) {
			throw new NoFreeResourceException();
		}

		s = constrain(s, getConstraintChecker(), t);

		if (s.isEmpty()) {
			throw new NoSuchResourceException();
		}
		else if (s.allOverloaded()) {
			change = false;
			cachedLoadState = true;
			cachedConstraints = t;
			hits = 0;
			throw new NoFreeResourceException();
		}
		else {
			cachedLoadState = false;
		}

		s = removeOverloaded(s);

		if (s.isEmpty()) {
			throw new NoFreeResourceException();
		}

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
				selected = s.last();
			}
		}
		else if (policy == POLICY_BEST_SCORE) {
			selected = s.last();
		}
		else {
			throw new KarajanRuntimeException("Invalid policy number: " + policy);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Next contact: " + selected);
		}

		sorted.changeLoad(selected, 1);
		selected.setDelayedDelta(successFactor);
		return selected.getHost();
	}

	public synchronized void releaseContact(Contact contact) {
		if (logger.isDebugEnabled()) {
			logger.debug("Releasing contact " + contact);
		}
		try {
			BoundContact bc = this.resolveVirtualContact(contact);
			super.releaseContact(contact);

			WeightedHost wh = sorted.findHost(bc);
			if (wh != null) {
				change = true;
				sorted.changeLoad(wh, -1);
				sorted.changeScore(wh, wh.getScore() + wh.getDelayedDelta());
			}
			else {
				logger.warn("ghost contact (" + contact + ") in releaseContact");
			}
		}
		catch (NoFreeResourceException e) {
			logger.warn("Failed to release contact " + contact, e);
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
				if (rcc.checkConstraints(wh.getHost(), tc)) {
					ns.add(wh);
				}
			}
			return ns;
		}
	}

	protected WeightedHostSet removeOverloaded(WeightedHostSet s) {
		if (s == sorted) {
			WeightedHostSet ns = new WeightedHostSet(scoreHighCap);
			Iterator i = s.iterator();
			while (i.hasNext()) {
				WeightedHost wh = (WeightedHost) i.next();
				if (!wh.isOverloaded()) {
					ns.add(wh);
				}
			}
			return ns;
		}
		else {
			Iterator i = s.iterator();
			while (i.hasNext()) {
				WeightedHost wh = (WeightedHost) i.next();
				if (wh.isOverloaded()) {
					i.remove();
				}
			}
			return s;
		}
	}

	private static String[] propertyNames;
	private static final String[] myPropertyNames = new String[] { POLICY,
			FACTOR_CONNECTION_REFUSED, FACTOR_CONNECTION_TIMEOUT, FACTOR_SUBMISSION_TASK_LOAD,
			FACTOR_TRANSFER_TASK_LOAD, FACTOR_FILEOP_TASK_LOAD, FACTOR_FAILURE, FACTOR_SUCCESS,
			SCORE_HIGH_CAP, JOB_THROTTLE, MAX_SUBMISSION_TIME };
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
			else if (JOB_THROTTLE.equals(name)) {
				defaultJobThrottle = floatThrottleValue(value);
			}
			else {
				double val = TypeUtil.toDouble(value);
				try {
					Field f = WeightedHostScoreScheduler.class.getDeclaredField(name);
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
			updateInternal();
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

			if (contacts == null) {
				return;
			}

			checkSubmissionTime(code, e.getStatus(), t, contacts);

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

	public static final String TASK_ATTR_SUBMISSION_TIME = "scheduler:submissionTime";

	private void checkSubmissionTime(int code, Status s, Task t, Contact[] contacts) {
		synchronized (t) {
			if (t.getType() == Task.JOB_SUBMISSION) {
				if (code == Status.SUBMITTING) {
					t.setAttribute(TASK_ATTR_SUBMISSION_TIME, s.getTime());
				}
				else {
					Date st = (Date) t.getAttribute(TASK_ATTR_SUBMISSION_TIME);
					if (st != null) {
						Date st2 = s.getTime();
						long submissionTime = st2.getTime() - st.getTime();
						t.setAttribute(TASK_ATTR_SUBMISSION_TIME, null);
						double delta = submissionTimeBias + submissionTimeFactor * submissionTime
								/ 1000;
						if (logger.isDebugEnabled()) {
							logger.debug("Submission time for " + t + ": " + submissionTime
									+ "ms. Score delta: " + delta);
						}
						factorMultiple(contacts, delta);
					}
				}
			}
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
				factorScore(wh, factor);
			}
		}
	}

	private void factorMultipleLater(Contact[] contacts, double factor) {
		for (int i = 0; i < contacts.length; i++) {
			BoundContact bc = (BoundContact) contacts[i];
			WeightedHost wh = sorted.findHost(bc);
			if (wh != null) {
				factorScoreLater(wh, factor);
			}
		}
	}
}
