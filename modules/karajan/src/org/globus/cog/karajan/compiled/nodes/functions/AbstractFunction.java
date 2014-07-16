// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 8, 2003
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class AbstractFunction extends InternalFunction {
	
	protected ChannelRef<Object> cr_vargs;
	
	@Override
	protected ArgInfo compileArgs(WrapperNode w, Signature sig, Scope scope)
			throws CompilationException {
		Var.Channel cr = scope.lookupChannel("...");
		cr_vargs = scope.getChannelRef(cr);
		return super.compileArgs(w, sig, scope);
	}

	@Override
	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		ret(stack, function(stack));
	}

	protected void ret(Stack stack, final Object value) {
		if (value != null) {
			if (value.getClass().isArray()) {
				try {
					Object[] array = (Object[]) value;
					for (int i = 0; i < array.length; i++) {
						cr_vargs.append(stack, array[i]);
					}
				}
				catch (ClassCastException e) {
					// array of primitives; return as is
					cr_vargs.append(stack, value);
				}
			}
			else {
				cr_vargs.append(stack, value);
			}
		}
	}
	
	protected boolean staticReturn(Scope scope, Object value) {
		Var.Channel crv = scope.parent.lookupChannel("...");
		return crv.append(value);
	}
	
	protected void returnDynamic(Scope scope) {
		Var.Channel crv = scope.parent.lookupChannel("...");
		crv.appendDynamic();
	}
	
	public abstract Object function(Stack stack);
}