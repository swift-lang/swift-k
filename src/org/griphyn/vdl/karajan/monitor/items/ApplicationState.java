/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 5, 2013
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.ArrayList;
import java.util.List;

public enum ApplicationState {
    //0
    INITIALIZING("Initializing"), SELECTING_SITE("Selecting site"), STAGE_IN("Stage in"),
    //3
    SUBMITTING("Submitting"), SUBMITTED("Submitted"), ACTIVE("Active"), STAGE_OUT("Stage out"),
    //7
    FAILED_BUT_CAN_RETRY("Retrying", "Failed but can retry"), REPLICATING("Replicating", "Replicating", false, 4 /* SUBMITTED */), 
    FINISHED_IN_PREVIOUS_RUN("Finished in previous run", "Finished in prev. run", false, 10),
    //10
    FINISHED_SUCCESSFULLY("Completed", "Finished successfully"),
    FAILED("Failed");
    
    private String name, logName;
    private boolean enabled;
    private int aliasIndex;
    
    private ApplicationState(String name) {
        this(name, name);
    }
    
    private ApplicationState(String name, String logName) {
        this(name, logName, true, -1);
    }
    
    private ApplicationState(String name, String logName, boolean enabled, int aliasIndex) {
        this.name = name;
        this.logName = logName;
        this.enabled = enabled;
        this.aliasIndex = aliasIndex;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLogName() {
        return logName;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public String toString() {
        return name;
    }
    
    private static ApplicationState[] enabledValues;
    
    public synchronized static ApplicationState[] enabledValues() {
        if (enabledValues == null) {
            List<ApplicationState> l = new ArrayList<ApplicationState>();
            for (ApplicationState s : values()) {
                if (s.isEnabled()) {
                    l.add(s);
                }
            }
            enabledValues = new ApplicationState[l.size()];
            l.toArray(enabledValues);
        }
        
        return enabledValues;
    }

    public boolean isTerminal() {
        return this == FAILED || this == FINISHED_SUCCESSFULLY || this == FINISHED_IN_PREVIOUS_RUN;
    }

    public int getAliasIndex() {
        if (aliasIndex == -1) {
            return ordinal();
        }
        else {
            return aliasIndex;
        }
    }
}