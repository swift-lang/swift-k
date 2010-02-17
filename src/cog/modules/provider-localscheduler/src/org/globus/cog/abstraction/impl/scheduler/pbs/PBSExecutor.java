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

	protected void writeScript(Writer wr, String exitcodefile, String stdout,
			String stderr) throws IOException {
		Task task = getTask();
		JobSpecification spec = getSpec();
		wr.write("#PBS -S /bin/bash\n");
		wr.write("#PBS -N " + task.getName() + '\n');
		wr.write("#PBS -m n\n");
		writeAttr("project", "-A ", wr);
		writeAttr("count", "-l nodes=", wr);
		writeWallTime(wr);
		writeAttr("queue", "-q ", wr);
		wr.write("#PBS -o " + quote(stdout) + '\n');
		wr.write("#PBS -e " + quote(stderr) + '\n');
		Iterator i = spec.getEnvironmentVariableNames().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			wr.write(name);
			wr.write('=');
			wr.write(quote(spec.getEnvironmentVariable(name)));
			wr.write('\n');
		}
		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
		boolean multiple = false; 
		if ("multiple".equals(type)) {		    
		    multiple = true;

		}
		
		if (multiple) {
            wr.write("NODES=`cat $PE_HOSTLIST`\n");
            wr.write("ECF=" + exitcodefile + "\n");
            wr.write("INDEX=0\n");
            wr.write("for NODE in $NODES; do\n");
            wr.write("  ssh $NODE /bin/bash -c \"");
        }
		if (type != null) {
			String wrapper = Properties.getProperties().getProperty(
					"wrapper." + type);
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
		List args = spec.getArgumentsAsList();
		if (args != null && args.size() > 0) {
			wr.write(' ');
			i = args.iterator();
			while (i.hasNext()) {
				wr.write(quote((String) i.next()));
				if (i.hasNext()) {
					wr.write(' ');
				}
			}
		}
		
		if (spec.getStdInput() != null) {
            wr.write(" < " + quote(spec.getStdInput()));
        }
		if (multiple) {
		    wr.write("; echo \\$? > $ECF.$INDEX\" &");
		}
		wr.write('\n');
		if (multiple) {
		    wr.write("  INDEX=$((INDEX + 1))\n");
		    wr.write("done\n");
            wr.write("wait\n");
            wr.write("EC=0\n");
            wr.write("INDEX=0\n");
            wr.write("PATH=PATH:/bin:/usr/bin\n");
            wr.write("for NODE in $NODES; do\n");
            wr.write("  touch $ECF.$INDEX\n");
            wr.write("  read TEC < $ECF.$INDEX\n");
            wr.write("  rm $ECF.$INDEX\n");
            wr.write("  if [ \"$EC\" = \"0\" -a \"$TEC\" != \"0\" ]; then\n");
            wr.write("    EC=$TEC\n");
            wr.write("    /bin/echo $EC > $ECF\n");
            wr.write("  fi\n");
            wr.write("  INDEX=$((INDEX + 1))\n");
		}
		else {
		    wr.write("/bin/echo $? >" + exitcodefile + '\n');
		}
		if (multiple) {
		    wr.write("done\n");
		}
		wr.close();
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
