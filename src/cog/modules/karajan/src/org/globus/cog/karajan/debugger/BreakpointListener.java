//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import org.globus.cog.karajan.util.ThreadedElement;

public interface BreakpointListener {

	void breakpointReached(ThreadedElement te);

	void stepReached(ThreadedElement te);

	void resumed(ThreadedElement te);
}
