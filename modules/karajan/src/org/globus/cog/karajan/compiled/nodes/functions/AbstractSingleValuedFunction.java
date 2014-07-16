//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 18, 2012
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;



public abstract class AbstractSingleValuedFunction extends AbstractFunction {

	protected Signature getSignature() {
		return new Signature(getParams());
	}

	@Override
	protected void resolveChannelReturns(WrapperNode w, Signature sig, Scope scope)
			throws CompilationException {
	    if (sig.getChannelReturns().isEmpty()) {
	        sig.addReturn(channel("...", 1));
	    }
		super.resolveChannelReturns(w, sig, scope);
	}

	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		scope.lookupChannel("...").appendDynamic();
		return super.compileBody(w, argScope, scope);
	}


	protected Param[] getParams() {
	    throw new RuntimeException("Default getParams() called");
	}
}
