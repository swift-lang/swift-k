// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 21, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;


public abstract class AbstractUParallelIterator extends AbstractParallelIterator {

	protected void addChannelBuffers(VariableStack stack) throws ExecutionException {
	}

	protected void closeBuffers(VariableStack stack) throws ExecutionException {
	}

	protected void initializeChannelBuffers(VariableStack stack) throws ExecutionException {
	}
	
}