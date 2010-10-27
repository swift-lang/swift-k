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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class PBSExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(PBSExecutor.class);

	public PBSExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	protected void writeAttr(String attrName, String arg, Writer wr)
			throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write("#PBS " + arg + String.valueOf(value) + '\n');
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

	protected void writeCountAndPPN(Writer wr, Properties properties) throws IOException {
	    Object count = getSpec().getAttribute("count");
	    Object ppn = getSpec().getAttribute("ppn");
	    if (count != null) {
	        if ("true".equals
                (properties.getProperty(Properties.USE_MPPWIDTH))) {
	                writeAttr("count", "-l mppwidth=", wr);
	        }
	        else {
	            wr.write("#PBS -l nodes=" + count + (ppn == null ? "" : ":ppn=" + ppn) + "\n");
	        }
	    }
	    else if (ppn != null) {
	        // I am unsure whether this is valid. However, I am also
	        // unsure whether the alternatives:
	        //   1. assuming count=1 when count is missing
	        //   2. not specifying PPN when count is missing
	        // ... are any better
	        wr.write("#PBS -l ppn=" + ppn + "\n");
	    }
	}

	protected void writeScript(Writer wr, String exitcodefile, String stdout,
			String stderr) throws IOException {
		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();
		wr.write("#PBS -S /bin/sh\n");
		wr.write("#PBS -N " + task.getName() + '\n');
		wr.write("#PBS -m n\n");
		writeAttr("project", "-A ", wr);
		writeCountAndPPN(wr, properties);
		writeWallTime(wr);
		writeAttr("queue", "-q ", wr);
		wr.write("#PBS -o " + quote(stdout) + '\n');
		wr.write("#PBS -e " + quote(stderr) + '\n');
		for (String name : spec.getEnvironmentVariableNames()) {
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write('\n');
		}
		if(spec.getEnvironmentVariableNames().size() > 0 )
		    wr.write("#PBS -v " + makeList(spec.getEnvironmentVariableNames()) + '\n');
		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
		boolean multiple = false; 
		if ("multiple".equals(type)) {		    
		    multiple = true;

		}
		
		if (multiple) {
		    writeMultiJobPreamble(wr, exitcodefile);
        }
		if (type != null) {
			String wrapper = 
			    properties.getProperty("wrapper." + type);
			if (logger.isDebugEnabled()) {
				logger.debug("Wrapper: " + wrapper);
			}
			if (wrapper != null) {
				wrapper = replaceVars(wrapper);
				wr.write(wrapper);
				wr.write(' ');
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Wrapper after variable substitution: " + wrapper);
			}
		}
		if (spec.getDirectory() != null) {
			wr.write("cd " + quote(spec.getDirectory()) + " && ");
		}
		
		wr.write(quote(spec.getExecutable()));
		List<String> args = spec.getArgumentsAsList();
		if (args != null && args.size() > 0) {
			wr.write(' ');
			Iterator<String> i = args.iterator();
			while (i.hasNext()) {
				wr.write(quote(i.next()));
				if (i.hasNext()) {
					wr.write(' ');
				}
			}
		}
		
		if (spec.getStdInput() != null) {
            wr.write(" < " + quote(spec.getStdInput()));
        }
		if (multiple) {
		    writeMultiJobPostamble(wr);
		}
		else {
		    wr.write('\n');
		    wr.write("/bin/echo $? >" + exitcodefile + '\n');
		}
		wr.close();
	}
	
	private String makeList(Collection<String> names) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = names.iterator();
        while (i.hasNext()) {
        	sb.append(i.next());
        	if (i.hasNext()) {
        		sb.append(", ");
        	}
        }
        return sb.toString();
    }

    protected void writeMultiJobPreamble(Writer wr, String exitcodefile)
            throws IOException {
        wr.write("NODES=`cat $PBS_NODEFILE`\n");
        wr.write("ECF=" + exitcodefile + "\n");
        wr.write("INDEX=0\n");
        wr.write("for NODE in $NODES; do\n");
        wr.write("  echo \"N\" >$ECF.$INDEX\n");
        wr.write("  ssh $NODE /bin/bash -c \\\" \"");
    }

	protected String getName() {
		return "PBS";
	}

	protected AbstractProperties getProperties() {
		return Properties.getProperties();
	}

	protected Job createJob(String jobid, String stdout,
			FileLocation stdOutputLocation, String stderr,
			FileLocation stdErrorLocation, String exitcode,
			AbstractExecutor executor) {
		return new Job(jobid, stdout, stdOutputLocation, stderr,
				stdErrorLocation, exitcode, executor);
	}
	
	private static QueuePoller poller;

	protected AbstractQueuePoller getQueuePoller() {
		synchronized(PBSExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}
}
