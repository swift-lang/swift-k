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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.StagingSetEntry;
import org.globus.cog.abstraction.interfaces.Task;

public class CondorExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(CondorExecutor.class);

	public CondorExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	protected void writeAttr(String attrName, String arg, Writer wr) throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write(arg + String.valueOf(value) + '\n');
		}
	}
	
	protected void writeScript(Writer wr, String exitcodefile, String stdout, String stderr) throws IOException {
		boolean grid = false;
		boolean nonshared = false;
		JobSpecification spec = getSpec();

		// Handle some predefined jobTypes
		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
		if ("MPI".equals(type)) {
			wr.write("universe = MPI\n");
		}
		else if("grid".equals(type)) {
			grid = true;
			String gridResource = (String) spec.getAttribute("gridResource");
			wr.write("universe = grid\n");
			wr.write("grid_resource = "+gridResource+"\n");
			// the below two lines are needed to cause the gridmonitor to be used
			// which is the point of all this...
			wr.write("stream_output = False\n");
			wr.write("stream_error  = False\n");
			wr.write("Transfer_Executable = false\n");
		}
		else {
			if(spec.getAttribute("condor.universe") == null) {
				wr.write("universe = vanilla\n");
			}
		}
		
		if (spec.getStageIn() != null || spec.getStageOut() != null) {
		    wr.write("should_transfer_files = YES\n");
		    wr.write("when_to_transfer_output = ON_EXIT_OR_EVICT\n");
		    wr.write("Transfer_Executable = false\n");
		    writeStaging(wr, spec);
		}

		if ("true".equals(spec.getAttribute("holdIsFailure"))) {
			wr.write("periodic_remove = JobStatus == 5\n");
		}
		
		writeAttr("count", "machine_count = ", wr);
		wr.write("output = " + quote(stdout) + '\n');
		wr.write("error = " + quote(stderr) + '\n');

		if (spec.getStdInput() != null) {
			wr.write("input = " + quote(spec.getStdInput()) + "\n");
		}

		Iterator<String> i = spec.getEnvironmentVariableNames().iterator();
		if (i.hasNext()) {
			wr.write("environment = ");
		}
		while (i.hasNext()) {
			String name = i.next();
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write(';');
		}
		wr.write("\n");

		if (spec.getDirectory() != null) {
			if(grid) {
				wr.write("remote_initialdir = " + quote(spec.getDirectory()) + "\n");
			}
			else if(!nonshared) {
				wr.write("initialdir = " + quote(spec.getDirectory()) + "\n");
			}
		}
        
		spec.getExecutable();
		wr.write("executable = " + quote(spec.getExecutable()) + "\n");
		List<String> args = spec.getArgumentsAsList();
		String wrapper = args.get(0);
		
		if (args != null && args.size() > 0) {
			wr.write("arguments = ");
			i = args.iterator();
			while (i.hasNext()) {
				wr.write(quote(i.next()));
				if (i.hasNext()) {
					wr.write(' ');
				}
			}
		}
 		wr.write('\n');

 		// Handle all condor attributes specified by the user
	    for(String a : spec.getAttributeNames()) {
	    	if(a != null && a.startsWith("condor.")) {
	    		String attributeName[] = a.split("condor.");
	    		wr.write(attributeName[1] + " = " + spec.getAttribute(a) + '\n');
	    	}
	    }

		wr.write("notification = Never\n");
		wr.write("leave_in_queue = TRUE\n");
		wr.write("queue\n");
		wr.close();
	}

	private void writeStaging(Writer wr, JobSpecification spec) throws IOException {
	    if (spec.getStageIn() != null) {
	        wr.write("transfer_input_files = ");
	        boolean first = true;
	        for (StagingSetEntry e : spec.getStageIn()) {
	            if (first) {
	                first = false;
	            }
	            else {
	                wr.write(",");
	            }
	            wr.write(e.getSource());
	        }
	        wr.write("\n");
	    }
	    
	    if (spec.getStageOut() != null) {
            wr.write("transfer_output_files = ");
            boolean first = true;
            for (StagingSetEntry e : spec.getStageOut()) {
                if (first) {
                    first = false;
                }
                else {
                    wr.write(",");
                }
                wr.write(e.getDestination());
            }
            wr.write("\n");
        }
    }

    private static final boolean[] TRIGGERS;

    static {
        TRIGGERS = new boolean[128];
        TRIGGERS[' '] = true;
        TRIGGERS['\n'] = true;
        TRIGGERS['\t'] = true;
        TRIGGERS['\\'] = true;
        TRIGGERS['>'] = true;
        TRIGGERS['<'] = true;
        TRIGGERS['"'] = true;
    }

    protected String quote(String s) {
        if ("".equals(s)) {
            return "";
        }
        boolean quotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 128 && TRIGGERS[c]) {
                quotes = true;
                break;
            }
        }
        if (!quotes) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        if (quotes) {
            sb.append('\\');
            sb.append('"');
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        if (quotes) {
            sb.append('\\');
            sb.append('"');
        }
        return sb.toString();
    }
    
	protected String getName() {
		return "Condor";
	}

	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, null, executor);
	}

	private static QueuePoller poller;

	protected AbstractQueuePoller getQueuePoller() {
		synchronized (CondorExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}

	protected String parseSubmitCommandOutput(String out) throws IOException {
	    out = out.trim();
		if (out.endsWith(".")) {
			out = out.substring(0, out.length() - 1);
		}
		int index = out.lastIndexOf(" ");
		return out.substring(index + 1);
	}

	public void cancel() throws TaskSubmissionException {
		String jobid = getJobid();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Marking job " + jobid
						+ " as removable from queue");
			}
			Process p = Runtime.getRuntime().exec(
					new String[] {
							getProperties()
									.getProperty(Properties.CONDOR_QEDIT),
							jobid, "LeaveJobInQueue", "FALSE" });
			int ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully marked " + jobid
							+ " as removable from queue");
				}
			}
			else {
				logger.warn("Failed to mark job " + jobid
						+ " as removable from queue: "
						+ getOutput(p.getInputStream()));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Cancelling job " + jobid);
			}

			p = Runtime.getRuntime().exec(
					new String[] { getProperties().getRemoveCommand(), jobid });
			ec = p.waitFor();
			if (ec == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully cancelled job " + jobid);
				}
			}
			else {
				logger.warn("Failed to cancel job " + jobid + ": "
						+ getOutput(p.getInputStream()));
			}
		}
		catch (Exception e) {
			logger.warn("Failed to cancel job " + jobid, e);
		}
	}
	
	public static void main(String[] args) {
	    String[] s = "a/b/c/d".split("/");
	    System.out.println(Arrays.asList(s));
	}
}
