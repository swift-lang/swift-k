// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 8, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;

public class Channel extends Node {
	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Var.Channel r = scope.lookupChannel("...");
		for (WrapperNode c : w.nodes()) {
			if (c.getNodeType().equals("k:var")) {
				r.append(new Param((String) c.getProperty(WrapperNode.TEXT), 
						Param.Type.CHANNEL));
			}
			else {
				throw new CompilationException(c, "Expected identifier");
			}
		}
		return null;
	}
}