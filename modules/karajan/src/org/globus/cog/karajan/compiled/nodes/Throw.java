// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 25, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;

public class Throw extends InternalFunction {
	private ArgRef<Object> exception;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("exception"));
	}

	@Override
	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		Object exc = exception.getValue(stack);
		if (exc instanceof String) {
			throw new ExecutionException(this, (String) exc);
		}
		else if (exc instanceof ExecutionException) {
			ExecutionException prev = (ExecutionException) exc;
			throw prev;
		}
		else if (exc instanceof Throwable) {
			Throwable t = (Throwable) exc;
			throw new ExecutionException(this, t.getMessage(), t);
		}
		else {
			throw new ExecutionException(this, String.valueOf(exc));
		}
	}
}