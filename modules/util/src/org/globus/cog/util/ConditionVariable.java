
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.util;

public class ConditionVariable { 
    private int value = 0;

    public ConditionVariable() {
    }

    public void setValue( int value ) {
    	this.value = value;
    }

    public int getValue() {
    	return this.value;
    }
}
