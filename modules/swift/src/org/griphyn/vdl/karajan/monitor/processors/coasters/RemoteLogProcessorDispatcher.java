//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2012
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import org.globus.cog.abstraction.coaster.rlog.RemoteLogHandler;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.processors.FilteringProcessorDispatcher;

public class RemoteLogProcessorDispatcher extends FilteringProcessorDispatcher {
    
    public RemoteLogProcessorDispatcher() {
    }

    @Override
    public String getSupportedSourceName() {
        return RemoteLogHandler.class.getSimpleName();
    }

    @Override
    public void initialize(SystemState state) {
        super.initialize(state);
        state.addItem(new CoasterStatusItem());
    }

    @Override
    public void processMessage(SystemState state, Object message, Object details) {
        super.processMessage(state, message, details);
    }
    
    
}
