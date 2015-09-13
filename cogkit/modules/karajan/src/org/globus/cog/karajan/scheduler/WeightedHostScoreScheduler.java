/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	public static final String DELAY_BASE = "delayBase";
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

	private double defaultDelayBase;

	private double submissionTimeBias, submissionTimeFactor;

	private boolean change;
	private TaskConstraints cachedConstraints;
	private boolean cachedLoadState;
	private int hits;
	private OverloadedHostMonitor monitor;
	
	private final Object lock = new Object();

	public WeightedHostScoreScheduler() {
		policy = POLICY_WEIGHTED_RANDOM;
		setDefaultFactors();
		monitor = new OverloadedHostMonitor(this);
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
		defaultDelayBase = 2;
		maxSubmissionTime = 20;
		updateInternal();
	}

	// This isn't very accurate since it varies by provider
	// and with submission parallelism
	// What it means is that if submission time is exactly this much
	// then a success should increase the score by successFactor
	public final double BASE_SUBMISSION_TIME = 0.1;

	protected void updateInternal() {
		if (maxSubmissionTime < BASE_SUBMISSION_TIME) {
			throw new IllegalArgumentException("maxSubmissionTime must be > "
					+ BASE_SUBMISSION_TIME);
		}
		submissionTimeFactor = -successFactor / (maxSubmissionTime - BASE_SUBMISSION_TIME);
		submissionTimeBias = -BASE_SUBMISSION_TIME * submissionTimeFactor;
	}

	public void setResources(ContactSet grid) {
		super.setResources(grid);
		if (grid.getContacts() == null) {
			return;
		}
		sorted = new WeightedHostSet(scoreHighCap, monitor);
		for (BoundContact contact : grid.getContacts()) {
			float thisJobThrottle = defaultJobThrottle;
			double thisDelayBase = defaultDelayBase;
			double thisInitialScore = 0;
			if (contact.hasProperty(JOB_THROTTLE)) {
				thisJobThrottle = floatThrottleValue(contact.getProperty(JOB_THROTTLE));
			}
			if (contact.hasProperty(DELAY_BASE)) {
				thisDelayBase = TypeUtil.toDouble(contact.getProperty(DELAY_BASE));
			}
			WeightedHost wh;
			if (contact.hasProperty(INITIAL_SCORE)) {
				thisInitialScore = TypeUtil.toDouble(contact.getProperty(INITIAL_SCORE));
			}
			wh = new WeightedHost(contact, thisInitialScore, 0, thisJobThrottle, thisDelayBase);
			addToSorted(wh);
		}
	}

	protected void addToSorted(WeightedHost wh) {
		sorted.add(wh);
	}
	
	protected WeightedHostSet getWeightedHostSet() {
	    return sorted;
	}

	protected void factorScore(WeightedHost wh, double factor) {
		double score = wh.getScore();
		if (logger.isDebugEnabled()) {
			logger.debug("multiplyScore(" + wh + ", " + factor + ")");
		}
		double ns = sorted.changeScoreDelta(wh, factor);
		if (logger.isDebugEnabled()) {
			logger.debug("Old score: " + WeightedHost.D4.format(score) + ", new score: "
					+ WeightedHost.D4.format(ns));
		}
	}

	protected void factorScoreLater(WeightedHost wh, double factor) {
		synchronized(lock) {
			if (logger.isDebugEnabled()) {
				logger.debug("factorLater(" + wh + ", " + factor + ")");
			}
			wh.setDelayedDelta(wh.getDelayedDelta() + factor);
		}
	}

	protected final double factor(double score, double factor) {
		return score + factor;
	}

	protected BoundContact getNextContact(TaskConstraints t)
			throws NoFreeResourceException {
		checkGlobalLoadConditions();

		earlyCheckCache(t);

		BoundContact contact;

		WeightedHostSet s = sorted;
		WeightedHost selected = null;

		if (s.allOverloaded()) {
			throw new NoFreeResourceException("All overloaded");
		}

		s = constrain(s, getConstraintChecker(), t);

		if (s.isEmpty()) {
			throw new NoSuchResourceException();
		}
		else {
			updateChachedState(s, t);
		}

		s = removeOverloaded(s);

		if (s.isEmpty()) {
			throw new NoFreeResourceException();
		}
		else if (s.size() == 1) {
			selected = s.iterator().next();
		}
		else {
			double sum = s.getSum();
			if (policy == POLICY_WEIGHTED_RANDOM) {
				double rand = Math.random() * sum;
				if (logger.isDebugEnabled() && !s.isEmpty()) {
					logger.debug("Sorted: " + s);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Rand: " + rand + ", sum: " + sum);
				}
				Iterator<WeightedHost> i = s.iterator();

				sum = 0;
				while (i.hasNext()) {
					WeightedHost wh = i.next();
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
				throw new RuntimeException("Invalid policy number: " + policy);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Next contact: " + selected);
		}

		sorted.changeLoad(selected, 1);
		selected.setDelayedDelta(successFactor);
		selected.notifyUsed();
		if (logger.isDebugEnabled()) {
			logger.debug("CONTACT_SELECTED host=" + selected.getHost() + ", score="
					+ WeightedHost.D4.format(selected.getTScore()));
		}
		return selected.getHost();
	}
	
	private void updateChachedState(WeightedHostSet s, TaskConstraints t) throws NoFreeResourceException {
		synchronized(lock) {
			if (s.allOverloaded()) {
				change = false;
				cachedLoadState = true;
				cachedConstraints = t;
				hits = 0;
				throw new NoFreeResourceException();
			}
			else {
				cachedLoadState = false;
			}
		}
	}

	private void earlyCheckCache(TaskConstraints t) throws NoFreeResourceException {
		synchronized(lock) {
			if (!change && cachedLoadState && cachedConstraints.equals(t)) {
				hits++;
				throw new NoFreeResourceException();
			}
		}
	}

	public boolean allOverloaded() {
		synchronized(lock) {
		    if (sorted == null) {
		        return false;
		    }
		    else {
		    	return sorted.allOverloaded();
		    }
		}
	}

	public void releaseContact(Contact contact) {
		synchronized(lock) {
			if (logger.isDebugEnabled()) {
				logger.debug("Releasing contact " + contact);
			}
			BoundContact bc = this.getBoundContact(contact);
			super.releaseContact(contact);
	
			if (bc == null) {
				logger.warn("Trying to release previously released contact: " + contact);
			}
			else {
				WeightedHost wh = sorted.findHost(bc);
				if (wh != null) {
					change = true;
					sorted.changeLoad(wh, -1);
					if (logger.isDebugEnabled()) {
						logger.debug("commitDelayedScore(" + wh + ", " + wh.getDelayedDelta());
					}
					sorted.changeScore(wh, wh.getScore() + wh.getDelayedDelta());
				}
				else {
					logger.warn("ghost contact (" + contact + ") in releaseContact");
				}
			}
		}
	}

	protected WeightedHostSet constrain(WeightedHostSet s, ResourceConstraintChecker rcc,
			TaskConstraints tc) {
		if (rcc == null) {
			return s;
		}
		else {
			return s.constrain(rcc, tc);
		}
	}

	protected WeightedHostSet removeOverloaded(WeightedHostSet s) {
		if (s == sorted) {
			WeightedHostSet ns = new WeightedHostSet(scoreHighCap);
			for (WeightedHost wh : s) {
				if (wh.isOverloaded() == 0) {
					ns.add(wh);
				}
			}
			return ns;
		}
		else {
			Iterator<WeightedHost> i = s.iterator();
			while (i.hasNext()) {
				WeightedHost wh = i.next();
				if (wh.isOverloaded() != 0) {
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
	private static Set<String> propertyNamesSet;

	static {
		propertyNamesSet = new HashSet<String>();
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
				else if ("best".equals(value)) {
					policy = POLICY_BEST_SCORE;
				}
				else {
					throw new RuntimeException("Unknown policy type: " + value);
				}
			}
			else if (JOB_THROTTLE.equals(name)) {
				defaultJobThrottle = floatThrottleValue(value);
			}
			else if (DELAY_BASE.equals(name)) {
				defaultDelayBase = TypeUtil.toDouble(value);
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
					throw new RuntimeException("Failed to set property '" + name + "'", e);
				}
			}
			updateInternal();
		}
		else {
			super.setProperty(name, value);
		}
	}

	@Override
	public void submitBoundToServices(Entry e, Contact[] contacts, Service[] services)
			throws TaskSubmissionException {
	    factorSubmission(e.task, contacts, 1);
		super.submitBoundToServices(e, contacts, services);
	}

	@Override
	public void statusChanged(StatusEvent se, Entry e) {
		try {
			Task t = e.task;
			int code = se.getStatus().getStatusCode();
			Contact[] contacts = e.contacts;

			if (contacts == null) {
				return;
			}

			checkSubmissionTime(code, se.getStatus(), t, contacts);

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
				Exception ex = se.getStatus().getException();
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
			super.statusChanged(se, e);
		}
	}

	public static final String TASK_ATTR_SUBMISSION_TIME = "scheduler:submissiontime";

	private void checkSubmissionTime(int code, Status s, Task t, Contact[] contacts) {
		synchronized (t) {
			if (t.getType() == Task.JOB_SUBMISSION) {
				if (code == Status.SUBMITTING) {
					t.setAttributeLC(TASK_ATTR_SUBMISSION_TIME, s.getTime());
				}
				else {
					Date st = (Date) t.getAttributeLC(TASK_ATTR_SUBMISSION_TIME);
					if (st != null) {
						Date st2 = s.getTime();
						long submissionTime = st2.getTime() - st.getTime();
						t.setAttributeLC(TASK_ATTR_SUBMISSION_TIME, null);
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
				// factorScoreLater(wh, factor);
				factorScore(wh, factor);
			}
		}
	}

	protected void removeOverloaded(WeightedHost wh) {
		sorted.removeOverloaded(wh);
		raiseTasksFinished();
	}

	protected void raiseTasksFinished() {
		synchronized(lock) {
			change = true;
			super.raiseTasksFinished();
		}
	}
}
