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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends Thread {
    public static final Logger logger = Logger.getLogger(QueuePoller.class);

    private String qstat;
    private LinkedList newjobs, donejobs;
    private Map jobs;
    boolean any = false;
    private int sleepTime;

    public QueuePoller() {
        setName("PBS-Local provider stream poller");
        setDaemon(true);
        jobs = new HashMap();
        newjobs = new LinkedList();
        donejobs = new LinkedList();
        sleepTime = Properties.getProperties().getPollInterval() * 1000;
        qstat = Properties.getProperties().getQStat();
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

    private static String[] cmdarray;

    protected void pollQueue() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Polling queue");
            }
            if (jobs.size() == 0) {
                return;
            }
            if (cmdarray == null || cmdarray.length != jobs.size() + 2) {
                cmdarray = new String[jobs.size() + 2];
                cmdarray[0] = qstat;
                cmdarray[1] = "-f";
            }
            Iterator i = jobs.keySet().iterator();
            int j = 2;
            while (i.hasNext()) {
                cmdarray[j] = (String) i.next();
                j++;
            }
            Process pqstat = Runtime.getRuntime().exec(cmdarray);
            int ec = pqstat.waitFor();
            if (ec != 0) {
                failAll("QStat failed (exit code " + ec + ")");
            }
            processStdout(pqstat.getInputStream());
            processStderr(pqstat.getErrorStream());
        }
        catch (Exception e) {
            failAll(e);
        }
    }

    protected void processStdout(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String currentJobID = null;
        Job currentJob = null;
        do {
            line = br.readLine();
            if (line != null) {
                line = line.trim();
                if (line.startsWith("Job Id: ")) {
                    currentJobID = line.substring("Job Id: ".length());
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
    }

    protected void processStderr(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        do {
            line = br.readLine();
            if (line != null) {
                line = line.trim();
                if (line.startsWith("qstat: Unknown Job Id ")) {
                    String jobid = line.substring("qstat: Unknown Job Id "
                            .length());
                    Job job = (Job) jobs.get(jobid);
                    if (job != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Status for " + jobid + " is Done");
                        }

                        /*
                         * The state setting is vetoable since the exit code file must
                         * be present on the disk before the job can be marked as done.
                         * This is done in order to be safe with the NFS case, but it also
                         * needs to be done so that it works with my PBS emulator ;)
                         */ 
                        job.setState(Job.STATE_DONE);
                        if (job.getState() == Job.STATE_DONE) {
                            donejobs.add(jobid);
                        }
                    }
                }
            }
        } while (line != null);
    }
}
