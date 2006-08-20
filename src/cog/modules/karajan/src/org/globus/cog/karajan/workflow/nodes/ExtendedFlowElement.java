//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 23, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

/**
 * Elements implementing this interface guarantee that
 * the time needed for execution is small, and therefore
 * can be invoked directly, without the need to resort to
 * events.
 */
public interface ExtendedFlowElement extends FlowElement {
	boolean isSimple();
	
	void executeSimple(VariableStack stack) throws ExecutionException;
}
