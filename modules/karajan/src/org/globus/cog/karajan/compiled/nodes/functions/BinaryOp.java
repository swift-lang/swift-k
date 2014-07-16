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

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class BinaryOp<T, R> extends AbstractFunction {
	protected ArgRef<T> v1;
	protected ArgRef<T> v2;

	@Override
	public R function(Stack stack) {
		return value(v1.getValue(stack), v2.getValue(stack));
	}

	protected abstract R value(T v1, T v2);

	@Override
	protected Signature getSignature() {
		return new Signature(params("v1", "v2"));
	}

	@Override
	protected void ret(Stack stack, Object value) {
		cr_vargs.append(stack, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		Var v1 = argScope.lookupParam("v1");
		Var v2 = argScope.lookupParam("v2");
		if (v1.getValue() != null && v2.getValue() != null) {
			if (staticReturn(scope, value((T) v1.getValue(), (T) v2.getValue()))) {
				return null;
			}
		}
		returnDynamic(scope);
		return this;
	}
}
