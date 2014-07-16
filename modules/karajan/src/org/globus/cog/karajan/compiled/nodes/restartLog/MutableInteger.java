//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

public class MutableInteger {
	private int value;

	public MutableInteger(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void inc() {
		value++;
	}
	
	public void dec() {
		value--;
	}
	
	public String toString() {
		return String.valueOf(value);
	}
}