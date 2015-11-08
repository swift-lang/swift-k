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

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class PBSExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(PBSExecutor.class);

	/**
	   PBS processes-per-node
	 */
	int ppn = 1;

	/**
	   Unique number for automatic task names
	*/
	private static int unique = 0; 
	
	public PBSExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	private static NumberFormat IDF = new DecimalFormat("000000");
	
	/** 
	    The job name is limited to 15 characters: 
		http://doesciencegrid.org/public/pbs/qsub.html
	 */
	protected void validate(Task task) {
		String name = task.getName();
		if (name == null) {
		    int i = 0;
            synchronized(PBSExecutor.class) {
                i = unique++;
            }
            name = "cog-" + IDF.format(i);
            if (logger.isDebugEnabled()) {
                logger.debug("PBS name: for: " + task.getIdentity() + 
                         " is: " + name);
            }
		}
		else if (name.length() > 15) {
		    task.setName(name.substring(0, 15));
		}
	}
	
	/** 
       Write attribute if non-null
       @throws IOException
	 */
	protected void writeAttr(String attrName, String arg, Writer wr)
	throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write("#PBS " + arg + String.valueOf(value) + '\n');
		}
	}

	/** 
       Write attribute if non-null and non-empty
       @throws IOException
	 */
	protected void writeNonEmptyAttr(String attrName, String arg, 
	                                 Writer wr)
	throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			String v = String.valueOf(value);
			if (v.length() > 0 )
				wr.write("#PBS " + arg + v + '\n');
		}
	}
	
	protected void writeWallTime(Writer wr) throws IOException {
		Object walltime = getSpec().getAttribute("maxwalltime");
		if (walltime != null) {
			wr.write("#PBS -l walltime="
					+ WallTime.normalize(walltime.toString(), "pbs-native")
					+ '\n');
		}
	}

	/**
	   Obtains profile settings regarding job size from
	   JobSpecification and writes them into the PBS file.
	   Looks for profiles count, ppn, ppts, and pbs.mpp
	   count: mandatory, default 1 (number of processes)
	   depth: default 1 (number of threads per node)
	   ppn: optional, default 1 (processes per node)
	   pbs.mpp: output mppwidth/mppnppn instead of nodes/ppn
	   pbs.properties: extra PBS properties
	   pbs.resource_list: extra PBS -l line

	   Note that the semantics are different for the pbs.mpp setting:
	   mppwidth is the total number of cores while nodes is the number
	   of nodes.

	   http://www.clusterresources.com/torquedocs/2.1jobsubmission.shtml
	   @return true if this is a multi-core job
	 */
	protected boolean writeCountAndPPN(JobSpecification spec, Writer wr)
	        throws IOException {
	    boolean multiple = false;

	    Object o;

	    // Number of program invocations
	    o = getSpec().getAttribute("count");
	    if (o != null) {
	        count = parseAndValidateInt(o, "count");
	    }
	    if (count != 1) {
	        multiple = true;
	    }

	    o = spec.getAttribute("ppn");
	    if (o != null) {
	        ppn = parseAndValidateInt(o, "ppn");
	    }

        o = spec.getAttribute("depth");
        if (o != null) {
            depth = parseAndValidateInt(o, "depth");
        }

	    String pbsProperties =
	        (String) getSpec().getAttribute("pbs.properties");

	    boolean mpp = false;
        if (getBoolean(spec.getAttribute("pbs.mpp"))) {
            mpp = true;
        }

	    StringBuilder sb = new StringBuilder(512);
	    sb.append("#PBS -l ");
	    if (mpp) {
	        sb.append("mppwidth=").append(count);
	        sb.append(",");
	        sb.append("mppnppn=").append(ppn);
	        sb.append(",");
	        sb.append("mppdepth=").append(depth);
	    }
	    else {
	        sb.append("nodes=");
	        sb.append(count);
	        sb.append(":");
	        sb.append("ppn=");
	        sb.append(ppn);
	    }

	    if (pbsProperties != null &&
	        pbsProperties.length() > 0 ) {
	        sb.append(":");
	        sb.append(pbsProperties);
	    }

	    sb.append('\n');

	    wr.write(sb.toString());

	    return multiple;
	}

	private boolean getBoolean(Object v) {
        if (v == null) {
            return false;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v).booleanValue();
        }
        if (v instanceof String) {
            return Boolean.valueOf((String) v);
        }
        throw new IllegalArgumentException("Invalid boolean value: " + v);
    }

    @Override
	protected void writeScript(Writer wr, String exitcodefile, String stdout, String stderr) 
	        throws IOException {
        
		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();

        getSpec().unpackProviderAttributes();

        validate(task);
        writeHeader(wr);
        
        String sJobType = (String) spec.getAttribute("jobType");
        if (logger.isDebugEnabled()) {
            logger.debug("Job type: " + sJobType);
        }
        RunMode runMode = getRunMode(sJobType);
        
        // aprun option specifically for Cray Beagle, Franklin
        // backwards compatibility
        if (spec.getAttribute("pbs.aprun") != null) {
            runMode = RunMode.APRUN;
        }
        
		wr.write("#PBS -S /bin/bash\n");
		wr.write("#PBS -N " + task.getName() + '\n');
		wr.write("#PBS -m n\n");
		writeNonEmptyAttr("project", "-A ", wr);
		writeCountAndPPN(spec, wr);

		writeWallTime(wr);
		writeNonEmptyAttr("queue", "-q ", wr);
		wr.write("#PBS -o " + quote(stdout + ".pbs") + '\n');
		wr.write("#PBS -e " + quote(stderr + ".pbs") + '\n');
		if (spec.getEnvironment().size() > 0) {
            wr.write("#PBS -v " + join(spec.getEnvironmentVariableNames(), ", ") + '\n');
        }

		for (EnvironmentVariable var : spec.getEnvironment()) {
			// "export" is necessary on the Cray XT5 Crow
			wr.write("export ");
			wr.write(var.getName());
			wr.write('=');
			wr.write(quote(var.getValue()));
			wr.write('\n');
		}


		String resources =
		    (String) spec.getAttribute("pbs.resource_list");
		if (resources != null && resources.length() > 0) {
		    if (logger.isDebugEnabled())
		        logger.debug("pbs.resource_list: " + resources);
		    wr.write("#PBS -l " + resources + '\n');
		}
		
		if (sJobType != null) {
            writeWrapper(wr, sJobType);
        }

		writePreamble(wr, runMode, "$PBS_NODEFILE", exitcodefile);
		writeCommand(wr, runMode);
	    writePostamble(wr, runMode, exitcodefile, stdout, stderr, ".pbs");
		
		wr.close();
	}


	@Override
	protected String getName() {
		return "PBS";
	}

	@Override
	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	@Override
	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, exitcode, executor);
	}

	private static QueuePoller poller;

	@Override
	protected AbstractQueuePoller getQueuePoller() {
		synchronized (PBSExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}
}
