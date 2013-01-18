// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Disallowed extends FlowNode {
	public void execute(VariableStack stack) throws ExecutionException {
		throw new ExecutionException("The use of " + this.getElementType()
				+ " is not allowed in a restricted environment.");
	}
}