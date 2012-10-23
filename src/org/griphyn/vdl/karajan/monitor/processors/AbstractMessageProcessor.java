//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 31, 2012
 */
package org.griphyn.vdl.karajan.monitor.processors;

public abstract class AbstractMessageProcessor implements LogMessageProcessor {

    protected abstract Class<?> getSupportedSource();
    
    @Override
    public String getSupportedSourceName() {
        return getSupportedSource().getName();
    }
    
}
