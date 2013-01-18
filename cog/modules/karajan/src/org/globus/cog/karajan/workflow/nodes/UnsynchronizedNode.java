//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 22, 2004
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;

public class UnsynchronizedNode extends Sequential {
	
	public UnsynchronizedNode() {
		setOptimize(false);
	}
	
	public void pre(VariableStack stack) throws ExecutionException {
		VariableStack copy = stack.copy();
		complete(stack);
		copy.enter();
		ThreadingContext.set(copy, ThreadingContext.get(copy).split(getIntProperty(UID)));
		super.pre(copy);
		super.executeChildren(copy);
	}
	
	protected void executeChildren(VariableStack stack) throws ExecutionException {
	}
	
	public void post(VariableStack stack) throws ExecutionException {
	}
}
