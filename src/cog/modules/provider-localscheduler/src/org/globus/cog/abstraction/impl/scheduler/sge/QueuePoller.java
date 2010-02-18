//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.sge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends AbstractQueuePoller {
    public static final Logger logger = Logger.getLogger(QueuePoller.class);

    private Set processed;

    public QueuePoller(AbstractProperties properties) {
        super("SGE provider queue poller", properties);
        processed = new HashSet();
    }

    private static String[] CMDARRAY;

    protected synchronized String[] getCMDArray() {
        if (CMDARRAY == null) {
            CMDARRAY = new String[] { getProperties().getPollCommand() };
        }
        return CMDARRAY;
    }

    // there's an XML options that the SGE qstat has. It's probably a safer
    // way to do this
    protected void processStdout(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String header = br.readLine();
        // sge qstat outputs nothing when there are no jobs
        if (header != null) {
            int jobIDIndex = header.indexOf("job-ID");
            int stateIndex = header.indexOf("state");
            // skip the -----
            br.readLine();
            processed.clear();
            do {
                line = br.readLine();
                if (line != null) {
                    String jobid = parseToWhitespace(line, jobIDIndex);
                    String state = parseToWhitespace(line, stateIndex);
                    if (jobid == null || jobid.equals("") || state == null
                            || state.equals("")) {
                        throw new IOException("Failed to parse qstat line: "
                                + line);
                    }
                    Job job = getJob(jobid);
                    if (job == null) {
                        continue;
                    }
                    processed.add(jobid);
                    if (state.contains("q") || state.contains("w")) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(jobid + " is queued");
                        }
                        job.setState(Job.STATE_QUEUED);
                    }
                    else if (state.contains("r")) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(jobid + " is running");
                        }
                        job.setState(Job.STATE_RUNNING);
                    }
                    else if (state.contains("E")) {
                        job.fail("Job is in an error state. Try running qstat -j "
                                    + jobid + " to see why.");
                    }
                }
            } while (line != null);
        }
        else {
            processed.clear();
        }
        Iterator i = getJobs().entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String id = (String) e.getKey();
            Job job = (Job) e.getValue();
            if (!processed.contains(id)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(id + " is done");
                }
                job.setState(Job.STATE_DONE);
                if (job.getState() == Job.STATE_DONE) {
                    addDoneJob(id);
                }
            }
            else {
                // at least on Ranger the job is done long
                // before qstat reports it as done, so check
                // if the exit code file is there
                File f = new File(job.getExitcodeFileName());
                if (f.exists()) {
                    job.setState(Job.STATE_DONE);
                    if (job.getState() == Job.STATE_DONE) {
                        addDoneJob(id);
                    }
                }
            }
        }
    }

    protected void processStderr(InputStream is) throws IOException {
    }
}
