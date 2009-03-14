//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 10, 2009
 */
package org.globus.cog.abstraction.impl.scheduler.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class AbstractQueuePoller implements Runnable {
	public static final Logger logger = Logger
			.getLogger(AbstractQueuePoller.class);

	public static final int MAX_CONSECUTIVE_FAILURES = 3;

	private String name;
	private LinkedList newjobs, donejobs;
	private Map jobs;
	boolean any = false;
	private int sleepTime;
	private int failures;

	private AbstractProperties properties;

	protected AbstractQueuePoller(String name, AbstractProperties properties) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new queue poll thread: " + name + "\n" + properties);
		}
		this.name = name;
		this.properties = properties;
		this.sleepTime = properties.getPollInterval() * 1000;
		jobs = new HashMap();
		newjobs = new LinkedList();
		donejobs = new LinkedList();
	}

	public void start() {
		if (logger.isDebugEnabled()) {
			logger.debug("Starring " + name + " poll thread.");
		}
		Thread t = new Thread(this);
		t.setName(name);
		t.setDaemon(true);
		t.start();
	}

	public void addJob(Job job) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding job '" + job.getJobID() + "' to poll thread");
		}
		synchronized (newjobs) {
			newjobs.add(job);
		}
	}

	public void run() {
		boolean empty;
		while (true) {
			while (jobs.size() + newjobs.size() == 0) {
				try {
					Thread.sleep(250);
				}
				catch (InterruptedException e) {
				}
			}
			Exception exc = null;
			pollQueue();
			if (logger.isInfoEnabled()) {
				logger.info("Active: " + jobs.size() + ", New: "
						+ newjobs.size() + ", Done: " + donejobs.size());
			}
			removeDoneJobs();
			commitNewJobs();
			try {
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e) {
			}
		}
	}

	protected void commitNewJobs() {
		if (newjobs.isEmpty()) {
			return;
		}
		else {
			synchronized (newjobs) {
				while (!newjobs.isEmpty()) {
					Job job = (Job) newjobs.removeFirst();
					jobs.put(job.getJobID(), job);
				}
			}
		}
	}

	protected void removeDoneJobs() {
		if (donejobs.isEmpty()) {
			return;
		}
		else {
			while (!donejobs.isEmpty()) {
				String jobid = (String) donejobs.removeFirst();
				removeDoneJob(jobid);
			}
		}
	}
	
	protected void removeDoneJob(String jobid) {
		getJob(jobid).setState(Job.STATE_DONE);
		jobs.remove(jobid);
	}

	protected void failAll(Exception e) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fail all ", e);
		}
		failAll(String.valueOf(e));
	}

	protected void failAll(String message) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fail all: " + message);
		}
		Iterator i = jobs.values().iterator();
		while (i.hasNext()) {
			Job job = (Job) i.next();
			try {
				job.fail(message);
			}
			catch (Exception e) {
				logger.warn("Could not fail job (" + job.getJobID() + ")", e);
			}
		}
		jobs.clear();
	}

	protected abstract String[] getCMDArray();

	protected void pollQueue() {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Polling queue");
			}
			if (jobs.size() == 0) {
				return;
			}

			String[] cmdarray = getCMDArray();
			if (logger.isDebugEnabled()) {
				logger.debug("Poll command: " + Arrays.asList(cmdarray));
			}
			Process pqstat = Runtime.getRuntime().exec(cmdarray);
			processStdout(pqstat.getInputStream());
			processStderr(pqstat.getErrorStream());
			int ec = pqstat.waitFor();
			if (ec != 0) {
				failures++;
				if (failures >= MAX_CONSECUTIVE_FAILURES) {
					failAll(getProperties().getPollCommandName()
							+ " failed (exit code " + ec + ")");
				}
			}
			else {
				failures = 0;
			}
		}
		catch (Exception e) {
			failAll(e);
		}
	}

	protected AbstractProperties getProperties() {
		return properties;
	}
	
	protected Job getJob(String id) {
		return (Job) jobs.get(id);
	}
	
	protected Map getJobs() {
		return jobs;
	}
	
	protected void addDoneJob(String id) {
		donejobs.add(id);
	}

	protected abstract void processStdout(InputStream is) throws IOException;

	protected abstract void processStderr(InputStream is) throws IOException;
}
