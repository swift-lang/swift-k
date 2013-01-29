//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 11, 2012
 */
package org.globus.cog.karajan.analyzer;

import org.globus.cog.karajan.parser.WrapperNode;

public class SingleValuedChannel extends StaticChannel {
	private final WrapperNode owner; 
	private Object value;
	private boolean set;
	
	public SingleValuedChannel(WrapperNode owner) {
		this.owner = owner;
	}

	@Override
	public boolean append(Object value) {
		if (set) {
			throw new IllegalArgumentException("Illegal extra argument to " + owner + " (" + value + ")");
		}
		if (dynamic) {
			return false;
		}
		this.value = value;
		set = true;
		return true;
	}

	public Object getValue() {
		return value;
	}
}
