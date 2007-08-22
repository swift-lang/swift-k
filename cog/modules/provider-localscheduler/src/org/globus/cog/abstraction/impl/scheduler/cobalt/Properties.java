//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.cobalt;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class Properties extends java.util.Properties {
	private static Logger logger = Logger.getLogger(Properties.class);

	public static final String PROPERTIES = "provider-cobalt.properties";
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String CQSUB = "cqsub";
	public static final String CQSTAT = "cqstat";
	public static final String EXITCODE_REGEXP = "exitcode.regexp";

	private static Properties properties;

	public static synchronized Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.load();
		}
		return properties;
	}

	private void load() {
		setDefaults();
		InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES);
		if (is == null) {
			logger.warn("Could not find " + PROPERTIES + ". Using defaults.");
		}
		else {
			try {
				super.load(is);
			}
			catch (IOException e) {
			}
		}
	}

	private void setDefaults() {
		setPollInterval(5);
		setCQSub("cqsub");
		setCQStat("cqstat");
		setExitcodeRegexp("(?BG/L job exit status = ([0-9]+))|(?exit status = \\(([0-9]+)\\))");
	}

	public void setPollInterval(int value) {
	    setProperty(POLL_INTERVAL, String.valueOf(value));
	}
	
	public int getPollInterval() {
	    return Integer.parseInt(getProperty(POLL_INTERVAL));
	}
	
	public void setCQSub(String cqsub) {
	    setProperty(CQSUB, cqsub);
	}
	
	public String getCQSub() {
	    return getProperty(CQSUB);
	}
	
	public void setCQStat(String cqstat) {
	    setProperty(CQSTAT, cqstat);
	}
	
	public String getCQStat() {
	    return getProperty(CQSTAT);
	}
	
	public String getExitcodeRegexp() {
		return getProperty(EXITCODE_REGEXP);
	}
	
	public void setExitcodeRegexp(String value) {
		setProperty(EXITCODE_REGEXP, value);
	}
}
