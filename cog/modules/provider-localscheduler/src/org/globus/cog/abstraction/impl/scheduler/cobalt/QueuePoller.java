//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.cobalt;

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

    private LinkedList newjobs, donejobs;
    private Map jobs;
    boolean any = false;
    private int sleepTime;

    public QueuePoller() {
        setName("Cobalt-Local provider stream poller");
        setDaemon(true);
        jobs = new HashMap();
        newjobs = new LinkedList();
        donejobs = new LinkedList();
        sleepTime = Properties.getProperties().getPollInterval() * 1000;
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

    public static final String[] CMDARRAY = new String[] {
            Properties.getProperties().getCQStat(), "-f" };

    protected void pollQueue() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Polling queue");
            }
            if (jobs.size() == 0) {
                return;
            }

            Process pqstat = Runtime.getRuntime().exec(CMDARRAY);
            int ec = pqstat.waitFor();
            if (ec != 0) {
                failAll("cqstat failed (exit code " + ec + ")");
            }
            processStdout(pqstat.getInputStream());
            processStderr(pqstat.getErrorStream());
        }
        catch (Exception e) {
            failAll(e);
        }
    }

    protected String parseToWhitespace(String s, int startindex) {
        for (int i = startindex; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return s.substring(startindex, i);
            }
        }
        return null;
    }

    private static Set processed = new HashSet();

    protected void processStdout(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String header = br.readLine();
        if (header == null) {
            throw new IOException("Failed to read cqstat header");
        }
        int jobIDIndex = header.indexOf("JobID");
        int stateIndex = header.indexOf("State");
        if (jobIDIndex == -1 || stateIndex == -1) {
            throw new IOException("Invalid cqstat header: " + header);
        }
        // skip the =====...
        br.readLine();
        processed.clear();
        do {
            line = br.readLine();
            if (line != null) {
                String jobid = parseToWhitespace(line, jobIDIndex);
                String state = parseToWhitespace(line, stateIndex);
                if (jobid == null || jobid.equals("") || state == null
                        || state.equals("")) {
                    throw new IOException("Failed to parse cqstat line: "
                            + line);
                }
                Job job = (Job) jobs.get(jobid);
                if (job == null) {
                    continue;
                }
                processed.add(job);
                if (state.equals("queued")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Status for " + jobid + " is Q");
                    }
                    job.setState(Job.STATE_QUEUED);
                }
                else if (state.equals("running")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Status for " + jobid + " is R");
                    }
                    job.setState(Job.STATE_RUNNING);
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
