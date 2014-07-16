//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2012
 */
package org.griphyn.vdl.karajan.monitor.processors;

import org.griphyn.vdl.karajan.monitor.SystemState;


public abstract class AbstractFilteringProcessor implements LogMessageProcessor {
    public abstract String getMessageHeader();

    @Override
    public final void processMessage(SystemState state, Object message, Object details) {
        throw new UnsupportedOperationException();
    }
    
    public abstract void processMessage(SystemState state, SimpleParser p, Object details);

    @Override
    public void initialize(SystemState state) {
    }
}
