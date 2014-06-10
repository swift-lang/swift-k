//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends AbstractQueuePoller {
	public static final Logger logger = Logger.getLogger(QueuePoller.class);


	public QueuePoller(Properties properties) {
		super(properties.getName() + "provider queue poller", properties);
	}

	private static String[] CMDARRAY;

	protected synchronized String[] getCMDArray() {
        String[] cmda = new String[1 + getJobs().size()];
        cmda[0] = getProperties().getPollCommand();
        int i = 1;
        for (Job j : getJobs().values()) {
            cmda[i++] = j.getJobID();
        }
        return cmda;
	}

    protected void processStdout(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		do {
			line = br.readLine();
			if (line != null) {
				try {
					String[] el = line.trim().split("\\s+", 4);
					String jobid = el[0];
					String message = null;
					
					if (el.length > 3) {
					    message = el[3];
					}
					
					Job job = getJob(jobid);
					if (job == null) {
					    logger.warn("Received status for an unknown job: '" + jobid + "'");
					}
					
					if (el[1].length() != 1) {
                        job.setMessage("Received unknown status code: '" + el[1] + "'");
                        job.setState(Job.STATE_FAILED);
                    }
					
					char status = el[1].charAt(0);
					if (logger.isDebugEnabled()) {
                        logger.debug("Status for " + jobid + " is " + status);
                    }
					
					if (el.length > 2) {
                        int exitCode = Integer.parseInt(el[2]);
                        job.setExitcode(exitCode);
                    }
					if (status == 'Q') {
					    job.setState(Job.STATE_QUEUED);
					}
					else if (status == 'R') {
					    job.setState(Job.STATE_RUNNING);
					}
					else if (status == 'C') {
					    job.setState(Job.STATE_DONE);
					}
					else if (status == 'F') {
					    if (message != null) {
					        job.setMessage(message);
					    }
					    else {
					        job.setMessage("Job failed");
					    }
					    job.setState(Job.STATE_FAILED);
					}
					
				}
				catch (Exception e) {
					logger.warn("Exception caught while handling "
							+ getProperties().getPollCommandName()
							+ " output: " + line, e);
				}
			}
		} while (line != null);
	}

	protected void processStderr(InputStream is) throws IOException {
	    // not used
	}
}
