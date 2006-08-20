//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 15, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

public interface ProcessListener {
	void processCompleted(int exitCode);
	
	void processFailed(String message);
}
