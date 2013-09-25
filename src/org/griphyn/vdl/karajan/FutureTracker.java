/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
