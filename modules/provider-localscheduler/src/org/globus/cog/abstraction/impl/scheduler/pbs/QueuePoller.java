//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.pbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends Thread {
    public static final Logger logger = Logger.getLogger(QueuePoller.class);
    
    public static final int MAX_CONSECUTIVE_FAILURES = 3;

    private LinkedList newjobs, donejobs;
    private Set processed;
    private Map jobs;
    boolean any = false;
    private int sleepTime;
    private int failures;

    public QueuePoller() {
        setName("PBS-Local provider stream poller");
        setDaemon(true);
        jobs = new HashMap();
        newjobs = new LinkedList();
        donejobs = new LinkedList();
        sleepTime = Properties.getProperties().getPollInterval() * 1000;
        processed = new HashSet();
    }

    public void addJob(Job job) {
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
                jobs.remove(jobid);
            }
        }
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

    private static final String[] QSTAT = new String[] {
            Properties.getProperties().getQStat(), "-f" };

    protected void pollQueue() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Polling queue");
            }
            if (jobs.size() == 0) {
                return;
            }
            Process pqstat = Runtime.getRuntime().exec(QSTAT);
            processStdout(pqstat.getInputStream());
            processStderr(pqstat.getErrorStream());
            int ec = pqstat.waitFor();
            if (ec != 0) {
                failures++;
                if (failures >= MAX_CONSECUTIVE_FAILURES) {
                    failAll("QStat failed (exit code " + ec + ")");
                }
            }
            else {
                failures = 0;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("QStat done");
            }
        }
        catch (Exception e) {
            failAll(e);
        }
    }

    protected void processStdout(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        processed.clear();
        String line;
        String currentJobID = null;
        Job currentJob = null;
        do {
            line = br.readLine();
            if (line != null) {
                line = line.trim();
                if (line.startsWith("Job Id: ")) {
                    currentJobID = line.substring("Job Id: ".length());
                    processed.add(currentJobID);
                    currentJob = (Job) jobs.get(currentJobID);
                    continue;
                }
                if (currentJob != null) {
                    if (line.startsWith("job_state = ")) {
                        switch (line.substring("job_state = ".length()).charAt(
                                0)) {
                            case 'Q': {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Status for " + currentJobID
                                            + " is Q");
                                }
                                currentJob.setState(Job.STATE_QUEUED);
                                break;
                            }
                            case 'R': {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Status for " + currentJobID
                                            + " is R");
                                }
                                currentJob.setState(Job.STATE_RUNNING);
                                break;
                            }
                        }
                    }
                }
            }
        } while (line != null);
        Iterator i = jobs.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String id = (String) e.getKey();
            if (!processed.contains(id)) {
                Job job = (Job) e.getValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("Status for " + id + " is Done");
                }
                job.setState(Job.STATE_DONE);
                if (job.getState() == Job.STATE_DONE) {
                    donejobs.add(id);
                }
            }
        }
    }

    protected void processStderr(InputStream is) throws IOException {
    }
}
