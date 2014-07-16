//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 7, 2012
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import org.apache.log4j.Level;
import org.globus.cog.abstraction.coaster.rlog.RemoteLogHandler;
import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.processors.AbstractFilteringProcessor;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;



public abstract class AbstractRemoteLogProcessor extends AbstractFilteringProcessor {
    
    @Override
    public Level getSupportedLevel() {
        return Level.INFO;
    }

    @Override
    public final String getSupportedSourceName() {
        return RemoteLogHandler.class.getName();
    }

    @Override
    public void processMessage(SystemState state, SimpleParser p, Object details) {
    }
}
