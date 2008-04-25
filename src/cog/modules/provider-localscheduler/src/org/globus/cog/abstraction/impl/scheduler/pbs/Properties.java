//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.pbs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class Properties extends java.util.Properties {
	private static Logger logger = Logger.getLogger(Properties.class);

	public static final String PROPERTIES = "provider-pbs.properties";
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String QSUB = "qsub";
	public static final String QSTAT = "qstat";
	public static final String QDEL = "qdel";

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
		setQSub("qsub");
		setQStat("qstat");
		setQDel("qdel");
	}

	public void setPollInterval(int value) {
	    setProperty(POLL_INTERVAL, String.valueOf(value));
	}
	
	public int getPollInterval() {
	    return Integer.parseInt(getProperty(POLL_INTERVAL));
	}
	
	public void setQSub(String qsub) {
	    setProperty(QSUB, qsub);
	}
	
	public String getQSub() {
	    return getProperty(QSUB);
	}
	
	public void setQStat(String qstat) {
	    setProperty(QSTAT, qstat);
	}
	
	public String getQStat() {
	    return getProperty(QSTAT);
	}
	
	public String getQDel() {
	    return getProperty(QDEL);
	}
	
	public void setQDel(String qdel) {
	    setProperty(QDEL, qdel);
	}
}
