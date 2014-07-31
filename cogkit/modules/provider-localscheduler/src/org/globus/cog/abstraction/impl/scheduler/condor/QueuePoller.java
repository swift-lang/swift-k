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
package org.globus.cog.abstraction.impl.scheduler.condor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends AbstractQueuePoller {
	public static final Logger logger = Logger.getLogger(QueuePoller.class);

	public QueuePoller(AbstractProperties properties) {
		super("Condor provider queue poller", properties);
	}

	private static String[] CMDARRAY;

	protected synchronized String[] getCMDArray() {
		if (CMDARRAY == null) {
			CMDARRAY = new String[] { getProperties().getPollCommand(),
					"-format", "\n%s", "ClusterId", "-format", " %d",
					"JobStatus", "-format", " %d", "ExitCode" };
		}
		return CMDARRAY;
	}

	protected void processStdout(InputStream is) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing condor_q stdout");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String currentJobID = null;
		Job currentJob = null;
		do {
			line = br.readLine();
			if (line != null) {
				try {
					line = line.trim();
					String[] el = line.split("\\s");
					currentJobID = el[0];
					currentJob = getJob(currentJobID);
					if (currentJob != null) {
						switch (el[1].charAt(0)) {
							case '5': {
								if (logger.isDebugEnabled()) {
									logger.debug("Status for " + currentJobID
											+ " is Held");
								}
								break;
							}
							case '1': {
								if (logger.isDebugEnabled()) {
									logger.debug("Status for " + currentJobID
											+ " is Queued");
								}
								currentJob.setState(Job.STATE_QUEUED);
								break;
							}
							case '2': {
								if (logger.isDebugEnabled()) {
									logger.debug("Status for " + currentJobID
											+ " is Running");
								}
								currentJob.setState(Job.STATE_RUNNING);
								break;
							}
							case '3': {
								if (logger.isDebugEnabled()) {
									logger.debug("Status for " + currentJobID
											+ " is Cancelled");
								}
								currentJob.fail("Job was cancelled");
								break;
							}
							case '4': {
								if (logger.isDebugEnabled()) {
									logger.debug("Status for " + currentJobID
											+ " is Completed");
								}
								try {
									currentJob.setExitcode(Integer
											.parseInt(el[2]));
								}
								catch (Exception e) {
									if (logger.isDebugEnabled()) {
										logger
												.debug("Could not get exit code from job. Line was: "
														+ line);
									}
								}
								addDoneJob(currentJob.getJobID());
								break;
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
	}

	protected void processStderr(InputStream is) throws IOException {
	}
	
	private String[] qeditargv;
	private String[] qrmargv;
	
	private void initArgvs() {
		if (qeditargv == null) {
			qeditargv = new String[] {
					getProperties().getProperty(Properties.CONDOR_QEDIT),
					null, "LeaveJobInQueue", "FALSE" };
			qrmargv = new String[] { getProperties().getRemoveCommand(),
					null };
		}
	}

	protected void removeDoneJob(String jobid) {
		super.removeDoneJob(jobid);
		try {
			initArgvs();
			if (logger.isDebugEnabled()) {
				logger.debug("Marking job " + jobid
						+ " as removable from queue");
			}
			qeditargv[1] = jobid;
			Process p = Runtime.getRuntime().exec(qeditargv);
			int ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully marked " + jobid
							+ " as removable from queue");
				}
			}
			else {
				logger.warn("Failed makr job " + jobid + " as removable from queue: "
						+ getOutput(p.getInputStream()));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Removing job " + jobid + " from queue");
			}
			qrmargv[1] = jobid;
			p = Runtime.getRuntime().exec(qrmargv);
			ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully removed job " + jobid
							+ " from queue");
				}
			}
			else {
				logger.warn("Failed to remove job " + jobid + " from queue: "
						+ getOutput(p.getInputStream()));
			}
		}
		catch (Exception e) {
			logger.warn("Failed to remove job " + jobid + " from queue", e);
		}
	}

	protected String getOutput(InputStream is) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Waiting for output from "
					+ getProperties().getRemoveCommandName());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String out = br.readLine();
		while (out != null) {
			sb.append(out);
			out = br.readLine();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Output from "
					+ getProperties().getRemoveCommandName() + " is: \""
					+ sb.toString() + "\"");
		}
		return sb.toString();
	}
}
