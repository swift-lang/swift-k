//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 20, 2006
 */
package org.globus.cog.karajan.scheduler;


public interface FailureHandler {
	boolean handleFailure(AbstractScheduler.Entry e, Scheduler s);
	
	void setProperty(String name, String value);
}
