//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.cobalt;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {
	private static Logger logger = Logger.getLogger(Properties.class);

	public static final String PROPERTIES = "provider-cobalt.properties";
	
	/*
	public static final String CQSUB = "cqsub";
	public static final String CQSTAT = "cqstat";
	public static final String CQDEL = "cqdel";
    */
	public static final String QSUB = "qsub";
	public static final String QSTAT = "qstat";
	public static final String QDEL = "qdel";
	public static final String EXITCODE_REGEXP = "exitcode.regexp";

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
		/*
        setSubmitCommand("cqsub");
		setPollCommand("cqstat");
		setRemoveCommand("cqdel");
        */
		setSubmitCommand("qsub");
		setPollCommand("qstat");
		setRemoveCommand("qdel");
		setExitcodeRegexp("(?:.*BG/. job exit status =\\s*([0-9]+))|(?:.*exit status = \\(([0-9]+)\\))");
	}

	
	public String getExitcodeRegexp() {
		return getProperty(EXITCODE_REGEXP);
	}
	
	public void setExitcodeRegexp(String r) {
		setProperty(EXITCODE_REGEXP, r);
	}

	public String getPollCommandName() {
		//return CQSTAT;
		return QSTAT;
	}

	public String getRemoveCommandName() {
		//return CQDEL;
		return QDEL;
	}

	public String getSubmitCommandName() {
		//return CQSUB;
		return QSUB;
	}		
}
