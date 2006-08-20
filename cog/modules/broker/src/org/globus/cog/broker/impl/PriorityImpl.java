//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.broker.impl;

import org.globus.cog.broker.interfaces.Priority;

public class PriorityImpl implements Priority {

    // default max priority
    private int maximumPriority = 100;
    
    // default min priority
    private int minimumPriority = 0;
    private int value = 0;

    public void setValue(int value) {
        this.value = value <= maximumPriority ? value : maximumPriority;
    }

    public int getValue() {
        return this.value;
    }

    public boolean equals(Object object) {
        return ((Priority) object).getValue() == this.value ? true : false;
    }

    public int hashCode() {
        return this.value;
    }

    public void setMaximumPriority(int maximumPriority) {
        this.maximumPriority = maximumPriority;
    }

    public int getMaximumPriority() {

        return this.maximumPriority;
    }

    public void setMinimumPriority(int minimumPriority) {
        this.minimumPriority = minimumPriority;
    }

    public int getMinimumPriority() {

        return this.minimumPriority;
    }

}