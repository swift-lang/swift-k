//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 21, 2013
 */
package org.griphyn.vdl.mapping;

import k.thr.LWThread;

public interface RootHandle extends DSHandle {
    void init(Mapper mapper);
    void setThread(LWThread thread);
    void setLine(int line);
    void setInput(boolean input);
    
    boolean isInput();
    LWThread getThread();
    int getLine();
    
    String getName();
    
    Mapper getActualMapper();
    boolean isArray();
    
    void mapperInitialized(Mapper mapper);
}
