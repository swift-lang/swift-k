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
 * Created on Mar 10, 2009
 */
package org.globus.cog.abstraction.impl.scheduler.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class AbstractQueuePoller implements Runnable {
    public static final Logger logger = Logger
        .getLogger(AbstractQueuePoller.class);

    public static final int MAX_CONSECUTIVE_FAILURES = 3;

    private String name;
    private LinkedList<Job> newjobs;
    private LinkedList<String> donejobs;
    private Map<String, Job> jobs;
    boolean any = false;
    private int sleepTime;
    private int failures;

    private AbstractProperties properties;

    protected AbstractQueuePoller(String name, AbstractProperties properties) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating new queue poll thread: " + name + "\n"
                    + properties);
        }
        this.name = name;
        this.properties = properties;
        this.sleepTime = properties.getPollInterval() * 1000;
        jobs = new HashMap<String, Job>();
        newjobs = new LinkedList<Job>();
        donejobs = new LinkedList<String>();
    }

    public void start() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting " + name + " poll thread.");
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
                logger.info("Actively monitored: " + jobs.size() + ", New: "
                        + newjobs.size() + ", Done: " + donejobs.size());
            }
            removeDoneJobs();
            commitNewJobs();
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {
                return;
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
                    Job job = newjobs.removeFirst();
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
                String jobid = donejobs.removeFirst();
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
        for (Job job : jobs.values()) {
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
            String stderr = readStderr(pqstat.getErrorStream());
            if (logger.isDebugEnabled()) {
                logger.debug("Stderr from poll command: " + stderr);
            }
            
            int ec = pqstat.waitFor();
            if (getError(ec, stderr) != 0) {
                failures++;
                if (failures >= MAX_CONSECUTIVE_FAILURES) {
                    failAll(getProperties().getPollCommandName()
                            + " failed (exit code " + ec + "): " + readStderr(pqstat.getErrorStream()));
                }
            }
            else {
                failures = 0;
            }
            
            // otherwise the pipes from the popen stay with the process
            // untill the Process object gets GC-ed
            pqstat.destroy();
        }
        catch (Exception e) {
            failAll(e);
        }
    }

    protected int getError(int ec, String stderr) {
        return ec;
    }

    private String readStderr(InputStream is) {
        StringBuffer sb = new StringBuffer();
        try {
        	BufferedReader br = new BufferedReader(new InputStreamReader(is));
        	String line = br.readLine();
        	while (line != null) {
        		sb.append(line);
        		sb.append('\n');
        		line = br.readLine();
        	}
        }
        catch (IOException e) {
        	sb.append(e);
        }
        return sb.toString();
    }

    protected AbstractProperties getProperties() {
        return properties;
    }

    protected Job getJob(String id) {
        return jobs.get(id);
    }

    protected Map<String, Job> getJobs() {
        return jobs;
    }

    protected void addDoneJob(String id) {
        donejobs.add(id);
    }

    protected abstract void processStdout(InputStream is) throws IOException;

    protected abstract void processStderr(InputStream is) throws IOException;

    protected String parseToWhitespace(String s, int startindex) {
    	for (int i = startindex; i < s.length(); i++) {
    		if (Character.isWhitespace(s.charAt(i))) {
    			return s.substring(startindex, i);
    		}
    	}
    	return null;
    }
}
