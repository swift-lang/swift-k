//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 7, 2015
 */
package org.globus.cog.karajan.compiled.nodes;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.compiled.nodes.user.InvocationWrapper;
import org.globus.cog.karajan.parser.WrapperNode;

public class TailCall extends Node {
	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		super.compile(w, scope);
		InvocationWrapper iw = (InvocationWrapper) w.getNode(0).compile(getParent(), scope);
		iw.setTailCall(true);
		return iw;
	}
}
