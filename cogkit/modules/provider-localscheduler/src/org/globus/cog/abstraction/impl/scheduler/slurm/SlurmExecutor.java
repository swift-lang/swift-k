package org.globus.cog.abstraction.impl.scheduler.slurm;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.slurm.Properties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;

public class SlurmExecutor extends AbstractExecutor {
	public static final Logger logger = Logger.getLogger(SlurmExecutor.class);

	// Number of program invocations
	private int count = 1;
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

	/**
	 * Verify that an object contains a valid int
	 * 
	 * @param obj
	 * @param name
	 * @return
	 */
	private int parseAndValidateInt(Object obj, String name) {
		try {
			assert (obj != null);
			return Integer.parseInt(obj.toString());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal value for " + name + ". Must be an integer.");
		}
	}

	@Override
	protected void writeScript(Writer wr, String exitcodefile, String stdout, String stderr) 
			throws IOException {

		Task task = getTask();
		JobSpecification spec = getSpec();
		Properties properties = Properties.getProperties();
		boolean exclusive_defined = false;

		validate(task);
		writeHeader(wr);

		String type = (String) spec.getAttribute("jobType");
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

        String mode = (String) spec.getAttribute("slurm.mode");
        // If mode is set to MPI, ntasks-per-node and cpus-per-task
        // must be handled manually via jobOptions.slurm.<option> = <value
        // This is necessary to avoid default tasks per node values
        // creating conflicts in submission requests
        if (! mode.equals("mpi")){
            wr.write("#DEBUG MPI MODE INVOKED! \n");
            if("single".equalsIgnoreCase(type)) {
                writeNonEmptyAttr("ppn", "--ntasks-per-node", wr);
            } else {
                wr.write("#SBATCH --ntasks-per-node=1\n");
                writeNonEmptyAttr("jobsPerNode", "--cpus-per-task", wr);
            }
        }

		// Handle all slurm attributes specified by the user
		for (String a : spec.getAttributeNames()) {
			if (a != null && a.startsWith("slurm.")) {
				String attributeName[] = a.split("slurm.");

                if (attributeName[1].equals("ranks")) {

                    //String ranks = spec.getAttribute(a);
                    wr.write("#SBATCH -N " + spec.getAttribute(a) + '\n');

                } else if (attributeName[1].equals("mode")) {
                    if (spec.getAttribute(a).equals("mpi")) {
                        type = "mpi";
                    }
                } else if (attributeName[1].equals("exclusive")) {
					exclusive_defined = true;
					if (spec.getAttribute(a).equals("true")) {
						wr.write("#SBATCH --exclusive");
					} else {
						wr.write("#SBATCH --share");
					}
				} else {
                    //wr.write("# DEBUG In else block <2> \n");
					wr.write("#SBATCH --" + attributeName[1] + "="
							+ spec.getAttribute(a) + '\n');
				}
			}
		}

		// Default to exclusive mode
		if (!exclusive_defined) {
			wr.write("#SBATCH --exclusive\n");
		}

		// Environment variables
		wr.write("\n");
		for (String name : spec.getEnvironmentVariableNames()) {
			wr.write("export " + name + '='
					+ quote(spec.getEnvironmentVariable(name)) + '\n');
		}

		// Determine wrapper
		if (type != null) {
			String wrapper = properties.getProperty("wrapper." + type);
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
        // For MPI jobs handle MPI lauch in app wrappers
        if("mpi".equalsIgnoreCase(type)) {
            wr.write("/bin/bash -c \'");
        } // Don't use srun for MPI jobs
		else if("single".equalsIgnoreCase(type)) {
			wr.write("/bin/bash -c \'");
		} else {
			wr.write("RUNCOMMAND=$( command -v ibrun || command -v srun )\n");
			wr.write("$RUNCOMMAND /bin/bash -c \'");
			//wr.write("/bin/bash -c \'");
		}

		if (spec.getDirectory() != null) {
			wr.write("cd " + quote(spec.getDirectory()) + " && ");
		}

		wr.write(quote(spec.getExecutable()));
		writeQuotedList(wr, spec.getArgumentsAsList());

		if (spec.getStdInput() != null) {
			wr.write(" < " + quote(spec.getStdInput()));
		}

		if ("multiple".equals(type)) {
			wr.write("; /bin/echo $? >" + exitcodefile + ".$SLURM_PROCID\'\n");
		} else {
			wr.write("; /bin/echo $? >" + exitcodefile + "\'\n");
		}
		wr.close();
	}

	void writeHeader(Writer writer) throws IOException {
		writer.write("#!/bin/bash\n\n");
		writer.write("#CoG This script generated by CoG\n");
		writer.write("#CoG   by class: " + SlurmExecutor.class + '\n');
		writer.write("#CoG   on date: " + new Date() + "\n\n");
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
