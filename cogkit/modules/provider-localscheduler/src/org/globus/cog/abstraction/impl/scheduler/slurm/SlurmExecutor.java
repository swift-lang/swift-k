package org.globus.cog.abstraction.impl.scheduler.slurm;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class SlurmExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(SlurmExecutor.class);

	private static NumberFormat IDF = new DecimalFormat("000000");

	// Used for task name generation
	private static int unique = 0;

	public SlurmExecutor(Task task, ProcessListener listener) {
		super(task, listener);
	}

	/**
	 * Write attribute if non-null
	 * 
	 * @throws IOException
	 */
	protected void writeAttr(String attrName, String arg, Writer wr)
			throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			wr.write("#SBATCH " + arg + "=" + String.valueOf(value) + '\n');
		}
	}

	/**
	 * Write attribute if non-null and non-empty
	 * 
	 * @throws IOException
	 */
	protected void writeNonEmptyAttr(String attrName, String arg, Writer wr)
			throws IOException {
		Object value = getSpec().getAttribute(attrName);
		if (value != null) {
			String v = String.valueOf(value);
			if (v.length() > 0)
				wr.write("#SBATCH " + arg + "=" + v + '\n');
		}
	}

	/**
	 * Write walltime in hh:mm:ss
	 * 
	 * @param wr
	 * @throws IOException
	 */
	protected void writeWallTime(Writer wr) throws IOException {
		Object walltime = getSpec().getAttribute("maxwalltime");
		if (walltime != null) {
			wr.write("#SBATCH --time="
					+ WallTime.normalize(walltime.toString(), "pbs-native")
					+ '\n');
		}
	}

    /**
     * Override for quotingLevel 
     */
    @Override
    protected int getQuotingLevel(RunMode runMode) {
        return 2;
    }

	/**
	 * Ensure tasks have a valid name
	 */
	protected void validate(Task task) {
		String name = task.getName();
		if (name == null) {
			int i = 0;
			synchronized (SlurmExecutor.class) {
				i = unique++;
			}
			name = "cog-" + IDF.format(i);
			task.setName(name);
		} else if (name.length() > 15) {
			task.setName(name.substring(0, 15));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Slurm name: for: " + task.getIdentity() + " is: " + name);
		}
	}
	
	@Override
	protected RunMode getRunMode(String jobType) {
        if (jobType == null) {
            return RunMode.SRUN_OR_IBRUN;
        }
        if (jobType.equals("single")) {
            return RunMode.PLAIN;
        }
        if (jobType.equals("multiple")) {
            return RunMode.SSH;
        }
        if (jobType.equals("MPI")) {
            return RunMode.SRUN;
        }
        throw new IllegalArgumentException("Unknown job type: " + jobType);
    }

	@Override
	protected void writeScript(Writer wr, String exitcodefile, String stdout, String stderr) 
			throws IOException {
		
		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();
		boolean exclusiveDefined = false;

		validate(task);
		writeHeader(wr);

		String sJobType = (String) spec.getAttribute("jobType");
        if (logger.isDebugEnabled()) {
            logger.debug("Job type: " + sJobType);
        }
        RunMode runMode = getRunMode(sJobType);
        
        if (spec.getAttribute("slurm.srun") != null) {
            runMode = RunMode.SRUN;
        }
        if (spec.getAttribute("slurm.ibrun") != null) {
            runMode = RunMode.IBRUN;
        }
        
		Object countValue = getSpec().getAttribute("count");
		
		if (countValue != null) {
			count = parseAndValidateInt(countValue, "count");
		}
		
		wr.write("#SBATCH --job-name=" + task.getName() + '\n');
		wr.write("#SBATCH --output=" + quote(stdout) + '\n');
		wr.write("#SBATCH --error=" + quote(stderr) + '\n');
		wr.write("#SBATCH --nodes=" + count + '\n');
		writeNonEmptyAttr("project", "--account", wr);
		writeNonEmptyAttr("queue", "--partition", wr);
		writeWallTime(wr);
		
	    if ("single".equalsIgnoreCase(sJobType)) {
			writeNonEmptyAttr("ppn", "--ntasks-per-node", wr);
	    }
	    else {
	    	wr.write("#SBATCH --ntasks-per-node=1\n");
	    	writeNonEmptyAttr("jobsPerNode", "--cpus-per-task", wr);
	    }

		// Handle all slurm attributes specified by the user
		for (String a : spec.getAttributeNames()) {
			if (a != null && a.startsWith("slurm.")) {
				String attributeName[] = a.split("slurm.");
				if (attributeName[1].equals("exclusive")) {
					exclusiveDefined = true;
					if (spec.getAttribute(a).equals("true")) {
						wr.write("#SBATCH --exclusive");
					} 
					else {
						wr.write("#SBATCH --share");
					}
				} 
				else {
					wr.write("#SBATCH --" + attributeName[1] + "="
							+ spec.getAttribute(a) + '\n');
				}
			}
		}

		// Default to exclusive mode
		if (!exclusiveDefined) {
			wr.write("#SBATCH --exclusive\n");
		}

		// Environment variables
		wr.write("\n");
		for (EnvironmentVariable var: spec.getEnvironment()) {
			wr.write("export " + var.getName() + '='
					+ quote(var.getValue()) + '\n');
		}

		if (sJobType != null) {
            writeWrapper(wr, sJobType);
        }

		writePreamble(wr, runMode, null, exitcodefile);
        writeCommand(wr, runMode);
        writePostamble(wr, runMode, exitcodefile, stdout, stderr);

		wr.close();
	}

	@Override
    protected void writeSSHPreamble(Writer wr, String nodeFile, String exitcodefile) throws IOException {
	    // SLURM has no node files but some strange environment variable
	    wr.write("NODES=`scontrol show hostname $SLURM_NODELIST`\n");
        wr.write("ECF=" + exitcodefile + "\n");
        wr.write("INDEX=0\n");
        wr.write("for NODE in $NODES; do\n");
        wr.write("  echo \"N\" >$ECF.$INDEX\n");
        wr.write("  ssh $NODE ");
    }

	@Override
	protected String getName() {
		return "Slurm";
	}

	@Override
	protected Properties getProperties() {
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
		synchronized (SlurmExecutor.class) {
			if (poller == null) {
				poller = new QueuePoller(getProperties());
				poller.start();
			}
			return poller;
		}
	}

	protected String parseSubmitCommandOutput(String out) throws IOException {
		if ("".equals(out)) {
			throw new IOException(getProperties().getSubmitCommandName()
					+ " returned an empty job ID");
		}
		String outArray[] = out.split(" ");
		return outArray[outArray.length - 1].trim();
	}

}
