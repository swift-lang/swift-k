//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2006
 */
package org.globus.cog.karajan.scheduler;

import org.globus.cog.abstraction.interfaces.Task;

public interface FailureHandler {
	boolean handleFailure(Task t, Scheduler s);
	
	void setProperty(String name, String value);
}
