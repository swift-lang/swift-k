//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Maybe extends SequentialChoice {
    
    public void completed(VariableStack stack) throws ExecutionException {
        startNext(stack);
    }
    
    
	public void failed(VariableStack stack, ExecutionException e) throws ExecutionException {
		complete(stack);
	}

	public void post(VariableStack stack) throws ExecutionException {
        commitBuffers(stack);
		complete(stack);
	}
}
