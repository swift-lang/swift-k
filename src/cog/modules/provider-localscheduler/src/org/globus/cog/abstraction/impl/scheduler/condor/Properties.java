//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.condor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {
	private static final long serialVersionUID = 1L;

	public static final String PROPERTIES = "provider-condor.properties";
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String CONDOR_SUBMIT = "condor_submit";
	public static final String CONDOR_Q = "condor_q";
	public static final String CONDOR_RM = "condor_rm";
	public static final String CONDOR_QEDIT = "condor_qedit";

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
		setSubmitCommand("condor_submit");
		setPollCommand("condor_q");
		setRemoveCommand("condor_rm");
		setProperty(CONDOR_QEDIT, "condor_qedit");
	}

	public String getPollCommandName() {
		return CONDOR_Q;
	}

	public String getRemoveCommandName() {
		return CONDOR_RM;
	}

	public String getSubmitCommandName() {
		return CONDOR_SUBMIT;
	}
	
	
}
