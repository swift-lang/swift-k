//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 18, 2012
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class ConstantOp<R> extends AbstractFunction {
	@Override
	public R function(Stack stack) {
		return value();
	}

	protected abstract R value();

	@Override
	protected Signature getSignature() {
		return new Signature(params());
	}

	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		staticReturn(scope, value());
		return null;
	}
}
