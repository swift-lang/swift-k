//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 19, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.io.IOException;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public class Input extends Sequential {
	public final void post(VariableStack stack) throws ExecutionException {
		//this thing never returns
		//TODO this is also broken, because it does not "java-return".
		while(true) {
			try {
				this.ret(stack, String.valueOf((char) System.in.read()));
			}
			catch (IOException e) {
				throw new ExecutionException("Could not read from stdin", e);
			}
		}
	}
}
