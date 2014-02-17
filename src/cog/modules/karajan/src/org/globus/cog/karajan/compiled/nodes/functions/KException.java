//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 26, 2006
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.util.TypeUtil;

public class KException extends AbstractSingleValuedFunction {
	private ChannelRef<Object> c_vargs;
    
	@Override
	protected Param[] getParams() {
		return params("...");
	}

	public Object function(Stack stack) {
		Channel<Object> args = c_vargs.get(stack);
		
		if (args.size() == 0) {
			throw new ExecutionException(this, "Missing argument(s)");
		}
		if (args.size() > 2) {
			throw new ExecutionException(this, "Too many arguments");
		}
		Object message = args.get(0);
		Throwable detail = null;
		if (args.size() == 2) {
			detail = (Throwable) args.get(1);
		}
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
