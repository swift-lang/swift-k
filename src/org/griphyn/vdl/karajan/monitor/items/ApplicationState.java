//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 5, 2013
 */
package org.griphyn.vdl.karajan.monitor.items;

public enum ApplicationState {
    INITIALIZING("Initializing"), SELECTING_SITE("Selecting site", "Sel. site"), STAGE_IN("Stage in"),
    SUBMITTING("Submitting"), SUBMITTED("Submitted"), ACTIVE("Active"), STAGE_OUT("Stage out"),
    FAILED("Failed"), REPLICATING("Replicating"), FINISHED_SUCCESSFULLY("Finished successfully", "Finished");
    
    private String name, shortName;
    
    private ApplicationState(String name) {
        this.name = name;
        this.shortName = name;
    }
    
    private ApplicationState(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
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

    public String toString() {
        return name;
    }
}