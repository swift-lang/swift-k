//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 26, 2006
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.util.TypeUtil;

public class KException extends AbstractSingleValuedFunction {
    private ArgRef<String> message;
    private ArgRef<Throwable> exception;
   
	@Override
	protected Param[] getParams() {
		return params("message", "exception");
	}

	public Object function(Stack stack) { 
		Object message = this.message.getValue(stack);
		Throwable detail = this.exception.getValue(stack);
		if (message instanceof String) {
			return new ExecutionException(this, (String) message, detail);
		}
		else if (message instanceof ExecutionException) {
			ExecutionException exc = (ExecutionException) message;
			return new ExecutionException(this, exc.getMessage(), exc);
		}
		else {
			return new ExecutionException(this, TypeUtil.toString(message));
		}
	}
}
