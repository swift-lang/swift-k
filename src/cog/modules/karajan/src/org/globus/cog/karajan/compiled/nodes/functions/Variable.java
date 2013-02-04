// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public class Variable extends Node {
	final static Logger logger = Logger.getLogger(Variable.class);

	private VarRef<Object> ref;
	private ChannelRef<Object> _vargs;

	@Override
	public void run(LWThread thr) {
		Stack stack = thr.getStack();
		_vargs.append(stack, ref.getValue(stack));
	}

	@Override
	public Node compile(WrapperNode wn, Scope scope) throws CompilationException {
		super.compile(wn, scope);
		String name = wn.getText();
		
		ref = scope.getVarRef(name);
		Var.Channel vargs = scope.lookupChannel(Param.VARGS);
		
		if (ref.isStatic()) {
			if (vargs.append(ref.getValue())) {
				return null;
			}
			else {
				_vargs = scope.getChannelRef(vargs);
				return this;
			}
		}
		else {
			vargs.appendDynamic();
			_vargs = scope.getChannelRef(vargs);
			return this;
		}
	}
}
