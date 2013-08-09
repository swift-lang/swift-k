//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2012
 */
package org.griphyn.vdl.karajan.monitor.processors.swift;

import org.griphyn.vdl.karajan.monitor.processors.FilteringProcessorDispatcher;

public class SwiftProcessorDispatcher extends FilteringProcessorDispatcher {
    
    public SwiftProcessorDispatcher() {
        super();
    }
    
    @Override
    public String getSupportedSourceName() {
        return "swift";
    }
}
