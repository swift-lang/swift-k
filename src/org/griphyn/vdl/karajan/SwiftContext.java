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

/*
 * Created on Sep 11, 2015
 */
package org.griphyn.vdl.karajan;

import java.util.concurrent.ConcurrentLinkedQueue;

import k.rt.Context;

import org.griphyn.vdl.karajan.lib.restartLog.RestartLogData;

public class SwiftContext extends Context {
    private RestartLogData restartLog;
    
    public SwiftContext() {
        super();
        setAttribute("RUN:CLEANUPS", new ConcurrentLinkedQueue<Object>());
    }

    public RestartLogData getRestartLog() {
        return restartLog;
    }

    public void setRestartLog(RestartLogData restartLog) {
        this.restartLog = restartLog;
    }
}
