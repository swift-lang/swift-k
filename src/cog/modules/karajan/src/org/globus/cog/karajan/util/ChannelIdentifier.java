//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 19, 2005
 */
package org.globus.cog.karajan.util;


public class ChannelIdentifier extends Identifier {
	private final boolean commutative;
	
	public ChannelIdentifier(String name) {
		this(name, false);
	}

	public ChannelIdentifier(String name, boolean commutative) {
		super(name);
		this.commutative = commutative;
	}

	public boolean isCommutative() {
		return commutative;
	}
}
