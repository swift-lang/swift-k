//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 15, 2010
 */
package org.globus.cog.karajan.workflow.futures;

import org.globus.cog.karajan.stack.VariableStack;

public interface FutureListener {
	void futureModified(Future f, VariableStack stack);
}
