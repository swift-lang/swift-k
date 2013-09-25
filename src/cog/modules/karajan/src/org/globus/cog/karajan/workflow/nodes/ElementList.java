
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 3, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.LinkedList;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;


public class ElementList extends FlowNode {

	public void execute(VariableStack stack) throws ExecutionException {
		ArgUtil.getVariableReturn(stack).append(new LinkedList(elements()));
		complete(stack);
	}
}
