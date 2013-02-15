//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 26, 2006
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class KException extends AbstractFunction {
	public static final Arg MESSAGE = new Arg.Positional("message");
	public static final Arg EXCEPTION = new Arg.Positional("exception");
	
	static {
		setArguments(KException.class, new Arg[] { MESSAGE, EXCEPTION });
	}

	public Object function(VariableStack stack) throws ExecutionException { 
		Object message = MESSAGE.getValue(stack, null);
		Throwable detail = (Throwable) EXCEPTION.getValue(stack, null);
		if (message instanceof String) {
			return new ExecutionException(stack.copy(), (String) message, detail);
		}
		else if (message instanceof ExecutionException) {
			ExecutionException exc = (ExecutionException) message;
			return new ExecutionException(stack.copy(), exc.getMessage(), exc);
		}
		else {
			return new ExecutionException(stack.copy(), TypeUtil.toString(message));
		}
	}
}
