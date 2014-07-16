//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public class CompilationException extends Exception {
	private String loc;
	
	public CompilationException(WrapperNode w, String msg, Throwable t) {
		super(msg, t);
		this.loc = w.toString();
	}
	
	public CompilationException(WrapperNode w, String msg) {
		super(msg);
		this.loc = w.toString();
	}
	
	public CompilationException(Node w, String msg) {
		super(msg);
		this.loc = w.toString();
	}

	@Override
	public String toString() {
		return loc + ": " + super.toString();
	}
	
	public String getLocation() {
		return loc;
	}
}
