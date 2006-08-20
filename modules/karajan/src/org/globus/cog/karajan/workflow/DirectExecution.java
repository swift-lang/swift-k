// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.workflow;

import org.globus.cog.karajan.stack.VariableStack;

public interface DirectExecution {
	public void start(VariableStack stack) throws ExecutionException;
}
