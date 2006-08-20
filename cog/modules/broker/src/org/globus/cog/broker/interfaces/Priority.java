//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.broker.interfaces;

public interface Priority {
    
    public void setValue(int value);
    public int getValue();
    
    public void setMaximumPriority(int maximumPriority);
    public int getMaximumPriority();
    
    public void setMinimumPriority(int minimumPriority);
    public int getMinimumPriority();
}
