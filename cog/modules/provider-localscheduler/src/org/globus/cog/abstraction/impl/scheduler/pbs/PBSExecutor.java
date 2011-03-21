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

	private int parseAndValidateInt(Object obj, String name) {
	    try {
	        assert(obj != null);
	        return Integer.parseInt(obj.toString());
	    }
	    catch (NumberFormatException e) {
	        throw new IllegalArgumentException("Illegal value for " + name + ". Must be an integer.");
	    }
	}

	/**
	   Obtains profile settings regarding job size from
	   JobSpecification and writes them into the PBS file.
	   Looks for profiles count, ppn, ppts, and mpp
	   count: mandatory, default 1 (number of cores)
	   ppn: optional, default 1 (cores per node)
	   pbs.mpp: output mppwidth/mppnppn instead of nodes/ppn
	   pbs.properties: extra PBS properties

	   Note that the semantics are different for the pbs.mpp setting:
	   mppwidth is the total number of cores while nodes is the number
	   of nodes.

	   http://www.clusterresources.com/torquedocs/2.1jobsubmission.shtml
	   @return true if this is a multi-core job
	 */
	protected boolean writeCountAndPPN(Writer wr) throws IOException {
	    boolean result = false;

	    Object o;

	    int count = 1;
	    o = getSpec().getAttribute("count");
	    if (o != null)
	        count = parseAndValidateInt(o, "count");
	    if (count != 1)
	        result = true;

	    o = getSpec().getAttribute("ppn");
	    int ppn = 1;
	    if (o != null)
	        ppn = parseAndValidateInt(o, "ppn");

	    String pbsProperties =
	        (String) getSpec().getAttribute("pbs.properties");

	    boolean mpp = false;
	    o = getSpec().getAttribute("pbs.mpp");
	    if (o != null)
	        mpp = parseAndValidateBool(o, "mpp");

	    if (count % ppn != 0)
	        throw new IllegalArgumentException
	        ("Count is not a multiple of ppn.");
	    int nodes = count / ppn;

	    StringBuilder sb = new StringBuilder(512);
	    sb.append("#PBS -l ");
	    if (mpp) {
	        sb.append("mppwidth=");
	        sb.append(count);
	        sb.append(":");
	        sb.append("mppnppn=");
	        sb.append(ppn);
	    }
	    else {
	        sb.append("nodes=");
	        sb.append(nodes);
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

	    return result;
	}

	private boolean parseAndValidateBool(Object obj, String name)
	{
	    try {
	        return Boolean.parseBoolean(obj.toString());
	    }
	    catch (NumberFormatException e) {
	        throw new IllegalArgumentException
	        ("Illegal value for " + name + ". Must be true/false.");
	    }
	}

	@Override
	protected void writeScript(Writer wr, String exitcodefile, String stdout,
			String stderr) throws IOException {
		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();

		wr.write("#PBS -S /bin/bash\n");
		wr.write("#PBS -N " + task.getName() + '\n');
		wr.write("#PBS -m n\n");
		writeAttr("project", "-A ", wr);
		boolean multiple = writeCountAndPPN(wr);
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

		if (spec.getEnvironmentVariableNames().size() > 0) {
		    wr.write("#PBS -v " + makeList(spec.getEnvironmentVariableNames()) + '\n');
		}

		String type = (String) spec.getAttribute("jobType");
		if (logger.isDebugEnabled()) {
			logger.debug("Job type: " + type);
		}
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
		synchronized(PBSExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}
}
