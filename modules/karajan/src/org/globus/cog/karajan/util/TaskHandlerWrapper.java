// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

import org.apache.log4j.Logger;

public class TaskHandlerWrapper {
	private static final Logger logger = Logger.getLogger(TaskHandlerWrapper.class);

	private String provider;

	private int type;

	public TaskHandlerWrapper() {
	}
	
	public TaskHandlerWrapper(String provider, int type) {
		this.provider = provider.toLowerCase();
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String toString() {
		return type + ":" + provider;
	}
}