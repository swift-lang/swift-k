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

package org.globus.cog.abstraction.impl.scheduler.lsf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends AbstractQueuePoller {
	public static final Logger logger = Logger.getLogger(QueuePoller.class);
	public static final int FULL_LIST_THRESHOLD = 16;

	private Set<String> processed;

	public QueuePoller(AbstractProperties properties) {
		super("LSF provider queue poller", properties);
		processed = new HashSet<String>();
	}

	private static String[] CMDARRAY;

	protected synchronized String[] getCMDArray() {
		if (CMDARRAY == null) {
			CMDARRAY = new String[] { getProperties().getPollCommand(), "-a" };
		}
		return CMDARRAY;
	}
	
	@Override
    protected int getError(int ec, String stderr) {
	    if (ec != 0) {
    	    BufferedReader sr = new BufferedReader(new StringReader(stderr));
    	    try {
        	    String line = sr.readLine();
        	    while (line != null) {
        	        if (!line.contains("is not found")) {
        	            return ec;
        	        }
        	        line = sr.readLine();
        	    }
    	    }
    	    catch (IOException e) {
    	        // should not occur while reading from a string reader
    	        e.printStackTrace();
    	    }
    	    return 0;
	    }
	    else {
	        return ec;
	    }
    }
	
	protected void processStdout(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String header = br.readLine();
		if (header == null) {
			logger.warn("Failed to read bjobs header");
			return;
		}
		int jobIDIndex = header.indexOf("JOBID");
		int stateIndex = header.indexOf("STAT");
		int locationIndex = header.indexOf("QUEUE");
		
		if (jobIDIndex == -1 || stateIndex == -1 || locationIndex == -1) {
			throw new IOException("Invalid bjobs header: " + header);
		}
		
		processed.clear();
		
		do {
			line = br.readLine();
			if (line != null) {
				String jobid = parseToWhitespace(line, jobIDIndex);
				String state = parseToWhitespace(line, stateIndex);
				if (jobid == null || jobid.equals("") || state == null || state.equals("")) {
                                	continue;
				}
				
				Job job = getJob(jobid);
				if (job == null){ continue; }
				processed.add(jobid);
				if (state.equals("PEND")) {
					if (logger.isDebugEnabled()) {
						logger.debug("Status for " + jobid + " is PEND");
					}
					job.setState(Job.STATE_QUEUED);
				}
				else if (state.equals("RUN")) {
					if (logger.isDebugEnabled()) {
						logger.debug("Status for " + jobid + " is RUN");
					}
					job.setState(Job.STATE_RUNNING);
				}
				else if (state.equals("DONE")) {
					if (logger.isDebugEnabled()) {
						logger.debug("Status for " + jobid + " is DONE");
					}
					addDoneJob(job.getJobID());
				}
				else if (state.equals("EXIT")) {
					if(logger.isDebugEnabled()) {
						logger.debug("Status for " + jobid + " is EXIT");
					}
					addDoneJob(job.getJobID());
				}
			}
		} while (line != null);
		
		
		Iterator<Entry<String, Job>> i = getJobs().entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, Job> e = i.next();
			String id = (String) e.getKey();
			if (!processed.contains(id)) {
				Job job = (Job) e.getValue();
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
	
	protected void processStderr(InputStream is) throws IOException {
	}
}
