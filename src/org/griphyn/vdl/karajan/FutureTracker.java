//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 1, 2011
 */
package org.griphyn.vdl.karajan;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.futures.Future;
import org.griphyn.vdl.mapping.DSHandle;

public class FutureTracker {
    public static final String VAR_NAME = "#swift:futureTracker";

    public static FutureTracker get(VariableStack stack) {
        return (FutureTracker) stack.firstFrame().getVar(VAR_NAME);
    }
    
    private static final FutureTracker ft = new FutureTracker();
    
    public static FutureTracker get() {
        return ft;
    }
    
    private Map<DSHandle, Future> futures;
    
    public FutureTracker() {
        futures = new HashMap<DSHandle, Future>();
    }

    public synchronized void add(DSHandle h, Future f) {
        futures.put(h, f);
    }

    public synchronized void remove(DSHandle h) {
        futures.remove(h);
    }

    public Map<DSHandle, Future> getMap() {
        return futures;
    }
    
    public synchronized Map<DSHandle, Future> getMapSafe() {
        return new HashMap<DSHandle, Future>(futures);
    }
}
