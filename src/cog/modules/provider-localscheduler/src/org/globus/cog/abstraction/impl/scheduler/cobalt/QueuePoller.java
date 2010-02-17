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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends AbstractQueuePoller {
	public static final Logger logger = Logger.getLogger(QueuePoller.class);
	
	public static final int MAX_INITIAL_WAIT_ROUNDS = 6;

	private Set processed;
	private Map ticks;
	
	public QueuePoller(AbstractProperties properties) {
		super("Cobalt provider queue poller", properties);
		processed = new HashSet();
		ticks = new HashMap();
	}

	protected void processStdout(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String header = br.readLine();
		if (header == null) {
			logger.warn("Failed to read cqstat header");
			return;
		}
		int jobIDIndex = header.indexOf("JobID");
		int stateIndex = header.indexOf("State");
		int locationIndex = header.indexOf("Location");
		if (jobIDIndex == -1 || stateIndex == -1 || locationIndex == -1) {
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
				String location = parseToWhitespace(line, locationIndex);
				if (jobid == null || jobid.equals("") || state == null
						|| state.equals("")) {
					throw new IOException("Failed to parse cqstat line: "
							+ line);
				}
				Job job = getJob(jobid);
				if (job == null) {
					continue;
				}
				processed.add(jobid);
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
					job.setLocation(location);
					job.setState(Job.STATE_RUNNING);
				}
			}
		} while (line != null);
		Iterator i = getJobs().entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			String id = (String) e.getKey();
			if (!processed.contains(id)) {
				Job job = (Job) e.getValue();
				if (job.getState() == job.STATE_NONE) {
				    int t = incTicks(id);
				    if (t <= MAX_INITIAL_WAIT_ROUNDS) {
				        continue;
				    }
				    ticks.remove(id);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Status for " + id + " is Done");
				}
				job.setState(Job.STATE_DONE);
				if (job.getState() == Job.STATE_DONE) {
					addDoneJob(id);
				}
			}
		}
	}

	private int incTicks(String id) {
        Integer i = (Integer) ticks.get(id);
        if (i == null) {
            i = new Integer(0);
        }
        ticks.put(id, new Integer(i.intValue() + 1));
        return i.intValue();
    }

    protected void processStderr(InputStream is) throws IOException {
	}

	public static String[] CMDARRAY;

	protected synchronized String[] getCMDArray() {
		if (CMDARRAY == null) {
			CMDARRAY = new String[] { getProperties().getPollCommand(), "-f" };
		}
		return CMDARRAY;
	}
}
