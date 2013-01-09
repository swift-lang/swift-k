package org.globus.cog.abstraction.impl.scheduler.slurm;

import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {

    private static final long serialVersionUID = 1L;
	public static final String PROPERTIES = "provider-slurm.properties";
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String SBATCH = "sbatch";
	public static final String SRUN = "srun";
	public static final String SQUEUE = "squeue";
	public static final String SCANCEL = "scancel";

	private static Properties properties;

	public static synchronized Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.load(PROPERTIES);
		}
		return properties;
	}
	
	protected void setDefaults() {
		setPollInterval(5);
		setSubmitCommand("sbatch");
		setPollCommand("squeue");
		setRemoveCommand("scancel");
		setRunCommand("srun");
	}

	public void setRunCommand(String val) {
		setProperty(getRunCommandName(), val);
	}

	public String getRunCommandName() {
		return SRUN;
	}

	public String getRunCommand() {
		return getProperty(getRunCommandName());
	}
	
	public String getPollCommandName() {
		return SQUEUE;
	}

	public String getRemoveCommandName() {
		return SCANCEL;
	}

	public String getSubmitCommandName() {
		return SBATCH;
	}
}
