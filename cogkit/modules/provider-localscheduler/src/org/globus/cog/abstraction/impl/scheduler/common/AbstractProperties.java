/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private String resolve(String var) {
        String v = null;
        if (var.startsWith("env.")) {
            v = System.getenv(var.substring(4));
        }
        else {
            v = System.getProperty(var);
        }
        if (v == null) {
            throw new IllegalArgumentException("No such system property or environment variable: '" + var + "'");
        }
        return v;
    }

    private String loadenv(String what) {
        int b = what.indexOf("${");
        while (b != -1) {
            int e = what.indexOf("}", b);
            String var = what.substring(b + 2, e);
            what = what.substring(0, b) + resolve(var) + what.substring(e + 1);
            b = what.indexOf("${");
        }
        return what;
    }

    protected void load(String name) {
        setDefaults();
        InputStream is = getClass().getClassLoader().getResourceAsStream(name);
        if (is == null) {
            logger.warn("Could not find " + name + ". Using defaults.");
        }
        else {
            try {
                super.load(is);
                for (String key: super.stringPropertyNames()){
                    String value = super.getProperty(key);
                    String resolved = loadenv(value);
                    super.setProperty(key, resolved);
                }
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
