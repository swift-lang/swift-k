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

public final class ListenerStackPair implements Runnable {
	public final FutureListener listener;
	public final VariableStack stack;
	
	public ListenerStackPair(FutureListener listener, VariableStack stack) {
		this.listener = listener;
		this.stack = stack;
	}

	public void run() {
	    listener.futureModified(null, stack);
	}
}
