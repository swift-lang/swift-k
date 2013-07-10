// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public class StringValue extends Node {
	private ChannelRef<Object> _vargs_r;
	private String value;

	@Override
	public void run(LWThread thr) {
		_vargs_r.append(thr.getStack(), value);
	}

	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		super.compile(w, scope);
		
		value = (String) w.getProperty(WrapperNode.TEXT);
		
		Var.Channel cv = scope.lookupChannel(Param.VARGS);	
		
		try {
			if (cv.append(value)) {
				return null;
			}
			else {
				_vargs_r = scope.getChannelRef(cv);
				return this;
			}
		}
		catch (IllegalArgumentException e) {
			throw new CompilationException(w, e.getMessage(), e);
		}
	}

	@Override
	public String getTextualName() {
		return '"' + value + '"';
	}
}