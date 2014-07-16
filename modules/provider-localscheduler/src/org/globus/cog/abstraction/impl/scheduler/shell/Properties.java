//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.shell;

// import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;

public class Properties extends AbstractProperties {

    private static final long serialVersionUID = 1L;
	
	public static final String POLL_INTERVAL = "poll.interval";
	public static final String SUBMIT_COMMAND = "submit.command";
	public static final String POLL_COMMAND = "poll.command";
	public static final String CANCEL_COMMAND = "cancel.command";
	public static final String NAME = "name";

	public static synchronized Properties getProperties(String name) {
	    Properties properties = new Properties();
		properties.load("provider-" + name + ".properties");
		properties.put(NAME, name);
		return properties;
	}
	
	protected void setDefaults() {
		setPollInterval(5);
	}


	public String getPollCommandName() {
		return POLL_COMMAND;
	}


	public String getRemoveCommandName() {
		return CANCEL_COMMAND;
	}


	public String getSubmitCommandName() {
		return SUBMIT_COMMAND;
	}

	public String getName() {
	    return getProperty(NAME);
	}
}
