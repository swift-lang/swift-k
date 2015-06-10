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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class LSFExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(LSFExecutor.class);

	public LSFExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}
	
	@Override
	  protected Job createJob(String jobid, String stdout,
				FileLocation stdOutputLocation, String stderr,
				FileLocation stdErrorLocation, String exitcode,
				AbstractExecutor executor) {
			return new Job(jobid, stdout, stdOutputLocation, stderr,
					stdErrorLocation, exitcode, executor);
		}
	
	@Override
	protected String getName() {
		return "LSF";
	}

	@Override
	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	/** 
    Write attribute if non-null and non-empty
    @throws IOException
	 */
	protected void writeNonEmptyAttr(String attrName, String arg,  Writer wr)
	        throws IOException {
		
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			String v = String.valueOf(value);
			if (v.length() > 0 )
				wr.write("#BSUB " + arg + " " + v + '\n');
		}
	}
	
	/**
	   @return true if this is a multi-core job
	**/
	protected boolean writeCountAndPPN(JobSpecification spec, Writer wr)
	        throws IOException {
		
	    boolean result = false;

	    Object o;

	    // Number of program invocations
	    o = getSpec().getAttribute("count");
	    if (o != null) {
	        count = parseAndValidateInt(o, "count");
	    }
	    if (count != 1) {
	        result = true;
	    }

	    wr.write("#BSUB -n " + count + "\n");
	    return result;
	}
	
	@Override
	protected void writeScript(Writer wr, String exitcodefile, String stdout, String stderr) 
	        throws IOException {
		
		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();
        validate(task);
        writeHeader(wr);
        
        String sJobType = (String) spec.getAttribute("jobType");
        if (logger.isDebugEnabled()) {
            logger.debug("Job type: " + sJobType);
        }
        RunMode runMode = getRunMode(sJobType);
        
		wr.write("#BSUB -L /bin/bash\n");
		wr.write("#BSUB -J " + task.getName() + '\n');
		wr.write("#BSUB -o " + quote(stdout) + '\n');
		wr.write("#BSUB -e " + quote(stderr) + '\n');
		
		writeNonEmptyAttr("project", "-P", wr);
		writeNonEmptyAttr("queue", "-q", wr);
		writeCountAndPPN(spec, wr);

        // Convert maxwalltime to HH:MM format
        int hours = 0;
        int minutes = 0;
        String walltime = getSpec().getAttribute("maxwalltime").toString();
        if (walltime != null) {
        	String walltimeSplit[] = walltime.split(":");
        	if (walltimeSplit.length == 1) {
        		minutes = Integer.valueOf(walltimeSplit[0]);
        	} 
        	else if (walltimeSplit.length > 1) {
        		hours = Integer.valueOf(walltimeSplit[0]);
        		minutes = Integer.valueOf(walltimeSplit[1]);
        		// LSF ignores seconds
        	}
        	if (minutes >= 60) {
                hours = minutes / 60;
                minutes = minutes % 60;
        	}
        	wr.write("#BSUB -W " + hours + ":" + minutes + '\n');
        }

        if (getSpec().getDirectory() != null) {
        	wr.write("#BSUB -cwd " + getSpec().getDirectory() + '\n');
        }
		
		for (String name : spec.getEnvironmentVariableNames()) {
			wr.write("export ");
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write('\n');
		}
		
		if (sJobType != null) {
            writeWrapper(wr, sJobType);
        }
        		
		writePreamble(wr, runMode, "$LSB_HOSTS", exitcodefile);
        writeCommand(wr, runMode);     
        writePostamble(wr, runMode, exitcodefile, stdout, stderr);
        
		wr.close();
	}
	
	@Override
    protected void writeCommand(Writer wr, RunMode runMode) throws IOException {
		int quotingLevel = getQuotingLevel(runMode);
        if (spec.getDirectory() != null) {
            wr.write("cd " + quote(spec.getDirectory(), quotingLevel) + " && ");
        }

        wr.write(quote(spec.getExecutable(), quotingLevel));
        writeQuotedList(wr, spec.getArgumentsAsList(), quotingLevel);
        
        // Handle all LSF attributes specified by the user
        for (String a : spec.getAttributeNames()) {
            if (a != null && a.startsWith("lsf.")) {
                String attributeName[] = a.split("lsf.");
                wr.write(attributeName[1] + " = " + spec.getAttribute(a) + '\n');
            }
        }
    
        if (spec.getStdInput() != null) {
            wr.write(" < " + quote(spec.getStdInput(), quotingLevel));
        }
    }

    protected void addAttr(String attrName, String option, List<String> l) {
		addAttr(attrName, option, l, null);
	}

	protected void addAttr(String attrName, String option, List<String> l, boolean round) {
		addAttr(attrName, option, l, null, round);
	}

	protected void addAttr(String attrName, String option, List<String> l, String defval) {
		addAttr(attrName, option, l, defval, false);
	}

	protected void addAttr(String attrName, String option, List<String> l,
			String defval, boolean round) {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			if (round) {
				value = round(value);
			}
			l.add(option);
			l.add(String.valueOf(value));
		}
		else if (defval != null) {
			l.add(option);
			l.add(defval);
		}
	}

	protected Object round(Object value) {
		if (value instanceof Number) {
			return new Integer(((Number) value).intValue());
		}
		else {
			return value;
		}
	}

    protected String parseSubmitCommandOutput(String out) throws IOException {
        if ("".equals(out)) {
            throw new IOException(getProperties().getSubmitCommandName()
                    + " returned an empty job ID");
        }
        String outArray[] = out.split(" ");
        String jobString = outArray[1];
        jobString = jobString.replaceAll("<", "");
        jobString = jobString.replaceAll(">", "");
        return jobString;
    }
    
    protected String[] buildCommandLine(File jobdir, File script,
            String exitcode, String stdout, String stderr)
    throws IOException {

        writeScript(new BufferedWriter(new FileWriter(script)), exitcode,
            stdout, stderr);
        if (logger.isDebugEnabled()) {
            logger.debug("Wrote " + getName() + " script to " + script);
        }

        String[] cmdline = { "/bin/bash", "-c", (getProperties().getSubmitCommand() + " < " + script.getAbsolutePath()) };
        return cmdline;
    }
    
	@Override
	protected void cleanup() {
		super.cleanup();
		new File(getStdout()).delete();
		new File(getStderr()).delete();
	}

	private static AbstractQueuePoller poller;

	@Override
	protected AbstractQueuePoller getQueuePoller() {
		synchronized(LSFExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}
}
