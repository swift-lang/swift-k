//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 14, 2005
 */
package org.globus.cog.karajan.workflow.service;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class RemoteContainer extends Sequential {

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		stack.setVar(RemoteNode.REMOTE_FLAG, true);
	}
	
}
