//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 28, 2013
 */
package org.globus.cog.karajan.analyzer;

import k.rt.Sink;

import org.globus.cog.karajan.compiled.nodes.Node;

public class InvalidArgChannel extends Sink<Object> {
	private final Node owner;
	
	public InvalidArgChannel(Node owner) {
		this.owner = owner;
	}
	
	@Override
	public boolean add(Object e) {
		throw new IllegalArgumentException("Illegal argument ('" + e + "' to " + owner + ")");
	}

	@Override
	public String toString() {
		return "<illegal>";
	}
}
