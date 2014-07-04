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
    INITIALIZING("Initializing"), SELECTING_SITE("Selecting site", "Sel. site"), STAGE_IN("Stage in"),
    //3
    SUBMITTING("Submitting"), SUBMITTED("Submitted"), ACTIVE("Active"), STAGE_OUT("Stage out"),
    //7
    FAILED("Failed"), REPLICATING("Replicating", "Replicating", false), 
    FINISHED_IN_PREVIOUS_RUN("Finished in previous run", "Finished in prev. run", false),
    //10
    FINISHED_SUCCESSFULLY("Finished successfully");
    
    private String name, shortName;
    private boolean enabled;
    
    private ApplicationState(String name) {
        this(name, name);
    }
    
    private ApplicationState(String name, String shortName) {
        this(name, name, true);
    }
    
    private ApplicationState(String name, String shortName, boolean enabled) {
        this.name = name;
        this.shortName = shortName;
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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
}