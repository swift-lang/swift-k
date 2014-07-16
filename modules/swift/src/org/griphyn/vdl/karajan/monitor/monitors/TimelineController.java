//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 13, 2011
 */
package org.griphyn.vdl.karajan.monitor.monitors;


public interface TimelineController {
    void setStartTime(long t);
    
    void setEndTime(long t);
    
    void addListener(Listener l);
    
    public interface Listener {
        void timeChanged(long t);
    }
}
