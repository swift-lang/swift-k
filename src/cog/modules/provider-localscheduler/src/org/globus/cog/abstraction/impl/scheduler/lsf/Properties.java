//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.scheduler.lsf;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {
	private static final long serialVersionUID = 1L;
	public static final String PROPERTIES = "provider-lsf.properties";
	
	
	public static final String BSUB = "bsub";
	public static final String BJOBS = "bjobs";
	public static final String BKILL = "bkill";

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
		setSubmitCommand("bsub");
		setPollCommand("bjobs");
		setRemoveCommand("bkill");
	}

	public String getPollCommandName() {
		return BJOBS;
	}

	public String getRemoveCommandName() {
		return BKILL;
	}

	public String getSubmitCommandName() {
		return BSUB;
	}
	
	
}
