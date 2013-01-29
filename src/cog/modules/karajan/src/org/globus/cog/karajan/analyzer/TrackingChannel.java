//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 11, 2012
 */
package org.globus.cog.karajan.analyzer;


public class TrackingChannel extends StaticChannel {
	private final StaticChannel prev;
	
	public TrackingChannel(StaticChannel prev) {
		this.prev = prev;
	}

	@Override
	public boolean append(Object value) {
		System.out.println(System.identityHashCode(this) + " < " + value);
		return prev.append(value);
	}

	@Override
	public String toString() {
		return "T" + super.toString();
	}
}
