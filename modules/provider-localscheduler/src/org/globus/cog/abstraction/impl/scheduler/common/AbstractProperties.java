//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 10, 2009
 */
package org.globus.cog.abstraction.impl.scheduler.common;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public abstract class AbstractProperties extends java.util.Properties {

	private static final long serialVersionUID = 1L;

	public static final Logger logger = Logger.getLogger(AbstractProperties.class);
    
    public static final String POLL_INTERVAL = "poll.interval";
    
    public static final String DEBUG = "debug";
    
    protected void load(String name) {
        setDefaults();
        InputStream is = getClass().getClassLoader().getResourceAsStream(name);
        if (is == null) {
            logger.warn("Could not find " + name + ". Using defaults.");
        }
        else {
            try {
                super.load(is);
            }
            catch (IOException e) {
            }
        }
    }
    
    protected abstract void setDefaults();
    
    public abstract String getSubmitCommandName();
    
    public abstract String getPollCommandName();
    
    public abstract String getRemoveCommandName();
    
	public String getSubmitCommand() {
		return getProperty(getSubmitCommandName());
	}
	
	public String getPollCommand() {
		return getProperty(getPollCommandName());
	}
	
	public String getRemoveCommand() {
		return getProperty(getRemoveCommandName());
	}
	
	public void setSubmitCommand(String cmd) {
		setProperty(getSubmitCommandName(), cmd);
	}
	
	public void setPollCommand(String cmd) {
        setProperty(getPollCommandName(), cmd);
    }
	
	public void setRemoveCommand(String cmd) {
        setProperty(getRemoveCommandName(), cmd);
    }
	
	public int getPollInterval() {
        return Integer.parseInt(getProperty(POLL_INTERVAL));
    }
	
	public void setPollInterval(int value) {
        setProperty(POLL_INTERVAL, String.valueOf(value));
    }

	public boolean isDebugEnabled() {
		return Boolean.valueOf(getProperty(DEBUG, "false")).booleanValue();
	}
}
