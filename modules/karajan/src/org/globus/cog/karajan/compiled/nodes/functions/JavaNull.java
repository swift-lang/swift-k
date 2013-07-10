//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 19, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;


public class JavaNull extends Node {

	private ChannelRef<Object> cr_vargs;
	
	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Node fn = super.compile(w, scope);
		Var.Channel vargs = scope.lookupChannel("...");
		if (vargs.append(null)) {
			return null;
		}
		else {
			cr_vargs = scope.getChannelRef(vargs);
			return fn;
		}
	}

	@Override
	public void run(LWThread thr) {
		cr_vargs.append(thr.getStack(), null);
	}
}
