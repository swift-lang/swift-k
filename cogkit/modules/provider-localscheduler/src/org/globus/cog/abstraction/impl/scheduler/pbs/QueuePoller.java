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
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.pbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
	public static final int FULL_LIST_THRESHOLD = 16;

	private Set processed;

	public QueuePoller(AbstractProperties properties) {
		super("PBS provider queue poller", properties);
		processed = new HashSet();
	}

	private static String[] CMDARRAY;

	protected synchronized String[] getCMDArray() {
	    if (getJobs().size() <= FULL_LIST_THRESHOLD) {
	        String[] cmda = new String[2 + getJobs().size()];
	        cmda[0] = getProperties().getPollCommand();
	        cmda[1] = "-f";
	        int i = 2;
	        for (Job j : getJobs().values()) {
	            cmda[i++] = j.getJobID();
	        }
	        return cmda;
	    }
	    else {
	        if (CMDARRAY == null) {
	            CMDARRAY = new String[] { getProperties().getPollCommand(), "-f" };
	        }
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
        	        if (!line.contains("Unknown Job Id")) {
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
		processed.clear();
		String line;
		String currentJobID = null;
		Job currentJob = null;
		do {
			line = br.readLine();
			if (line != null) {
				try {
					line = line.trim();
					if (line.startsWith("Job Id: ")) {
						currentJobID = line.substring("Job Id: ".length());
						processed.add(currentJobID);
						currentJob = getJob(currentJobID);
						continue;
					}
					if (currentJob != null) {
						if (line.startsWith("job_state = ")) {
						    if (logger.isDebugEnabled()) {
						        logger.debug("Status line: " + line);
						    }
							switch (line.substring("job_state = ".length())
									.charAt(0)) {
								case 'Q': {
									if (logger.isDebugEnabled()) {
										logger.debug("Status for "
												+ currentJobID + " is Q");
									}
									currentJob.setState(Job.STATE_QUEUED);
									break;
								}
								case 'R': {
									if (logger.isDebugEnabled()) {
										logger.debug("Status for "
												+ currentJobID + " is R");
									}
									currentJob.setState(Job.STATE_RUNNING);
									break;
								}
								case 'C': {
									// for sites where keep_completed is there,
									// don't wait
									// for the job to be removed from the queue
									if (logger.isDebugEnabled()) {
										logger.debug("Status for "
												+ currentJobID + " is C");
									}
									addDoneJob(currentJob.getJobID());
									break;
								}
							}
						}
						else if (line.startsWith("exit_status = ")) {
							try {
								int ec = Integer.parseInt(line.substring(
										"exit_status = ".length()).trim());
								currentJob.setExitcode(ec);
							}
							catch (Exception e) {
								if (logger.isDebugEnabled()) {
									logger.debug("Could not parse exit_status",
											e);
								}
							}
						}
					}
				}
				catch (Exception e) {
					logger.warn("Exception caught while handling "
							+ getProperties().getPollCommandName()
							+ " output: " + line, e);
				}
			}
		} while (line != null);
		Iterator i = getJobs().entrySet().iterator();
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
					addDoneJob(id);
				}
			}
		}
	}

	protected void processStderr(InputStream is) throws IOException {
	}
}
