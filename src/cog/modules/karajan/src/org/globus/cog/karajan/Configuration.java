//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 30, 2005
 */
package org.globus.cog.karajan;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
	public static final String WRITE_INTERMEDIATE_SOURCE = "intermediate";
	public static final String DEBUGGER = "debugger";
	public static final String DEBUG = "debug";
	public static final String SHOW_STATISTICS = "showstats";
	public static final String DUMP_STATE_ON_ERROR = "dumpstate";

	private static Configuration configuration;
	
	public synchronized static Configuration getDefault() {
		if (configuration == null) {
			configuration = new Configuration();
		}
		return configuration;
	}
	
	private final Map options;
	
	private Configuration() {
		options = new HashMap();
	}

	public boolean getFlag(String name) {
		Boolean flag = (Boolean) options.get(name);
		if (flag != null) {
			return flag.booleanValue();
		}
		else {
			return false;
		}
	}
	
	public String getString(String name) {
		return (String) options.get(name);
	}
	
	public Object get(String name) {
		return options.get(name);
	}
	
	public void set(String name, Object value) {
		options.put(name, value);
	}
	
	public void set(String name, boolean value) {
		options.put(name, Boolean.valueOf(value));
	}
}
