package org.globus.cog.abstraction.impl.scheduler.slurm;

import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {

    private static final long serialVersionUID = 1L;
	public static final String PROPERTIES = "provider-slurm.properties";
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String QSUB = "qsub";
	public static final String QSTAT = "qstat";
	public static final String QDEL = "qdel";
	public static final String USE_MPPWIDTH = "use.mppwidth";

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
		setSubmitCommand("qsub");
		setPollCommand("qstat");
		setRemoveCommand("qdel");
	}


	public String getPollCommandName() {
		return QSTAT;
	}


	public String getRemoveCommandName() {
		return QDEL;
	}


	public String getSubmitCommandName() {
		return QSUB;
	}
}
