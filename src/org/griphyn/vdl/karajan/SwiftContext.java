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
import org.griphyn.vdl.util.RootFS;

public class SwiftContext extends Context {
    public static final String ATTR_SWIFT_CONFIG = "SWIFT:CONFIG";
    public static final String ATTR_SWIFT_SCRIPT_NAME = "SWIFT:SCRIPT_NAME";
    public static final String ATTR_SWIFT_RUN_ID = "SWIFT:RUN_ID";
    public static final String ATTR_SWIFT_DRY_RUN = "SWIFT:DRY_RUN";
    public static final String ATTR_SWIFT_HOME = "SWIFT:HOME";
    public static final String ATTR_SWIFT_ROOT_FS = "SWIFT:ROOT_FS";
    public static final String ATTR_SWIFT_DEBUG_DIR_PREFIX = "SWIFT:DEBUG_DIR_PREFIX";
    public static final String ATTR_SWIFT_CWD = "SWIFT:CWD";
    public static final String ATTR_RUN_CLEANUPS = "RUN:CLEANUPS";
    public static final String ATTR_RUN_ERRORS = "RUN:ERRORS";
    public static final String ATTR_RUN_WARNINGS = "RUN:WARNINGS";
    
    private RestartLogData restartLog;
    
    public SwiftContext() {
        super();
        setAttribute(ATTR_RUN_CLEANUPS, new ConcurrentLinkedQueue<Object>());
    }

    public RestartLogData getRestartLog() {
        return restartLog;
    }

    public void setRestartLog(RestartLogData restartLog) {
        this.restartLog = restartLog;
    }

    public RootFS getRootFS() {
        return (RootFS) getAttribute(ATTR_SWIFT_ROOT_FS);
    }
}
