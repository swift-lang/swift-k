//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 12, 2014
 */
package org.globus.cog.abstraction.interfaces;

import java.util.EnumSet;


public class TaskHandlerCapabilities {
    public enum Key {
        THIRD_PARTY_TRANSFERS, SIMULATED_THIRD_PARTY_TRANSFERS,
        SIMPLE_FILE_STAGING, JOB_DIR_STAGING, FULL_FILE_STAGING, FILE_CLEANUP 
    }
    
    public static final TaskHandlerCapabilities PLAIN_TRANSFER = 
        new TaskHandlerCapabilities(Key.SIMULATED_THIRD_PARTY_TRANSFERS);
    public static final TaskHandlerCapabilities PLAIN_FILEOP = 
        new TaskHandlerCapabilities();
    public static final TaskHandlerCapabilities TRANSFER_WITH_3RD_PARTY = 
        new TaskHandlerCapabilities(Key.THIRD_PARTY_TRANSFERS);
    public static final TaskHandlerCapabilities EXEC_NO_STAGING = 
        new TaskHandlerCapabilities();
    public static final TaskHandlerCapabilities EXEC_IN_JOB_DIR_STAGING = 
        new TaskHandlerCapabilities(Key.JOB_DIR_STAGING);
    public static final TaskHandlerCapabilities EXEC_SIMPLE_STAGING = 
        new TaskHandlerCapabilities(Key.SIMPLE_FILE_STAGING);
    public static final TaskHandlerCapabilities EXEC_FULL_STAGING_AND_CLEANUP = 
        new TaskHandlerCapabilities(Key.FULL_FILE_STAGING, Key.FILE_CLEANUP);
    public static final TaskHandlerCapabilities NONE = 
        new TaskHandlerCapabilities();
    
    private EnumSet<Key> s;
    
    public TaskHandlerCapabilities(Key... keys) {
        this.s = EnumSet.noneOf(Key.class);
        for (Key k : keys) {
            this.s.add(k);
        }
    }
    
    public TaskHandlerCapabilities(EnumSet<Key> s) {
        this.s = s;
    }
    
    public boolean supportsFeature(Key k) {
        return s.contains(k);
    }

    public boolean supportsAnyOf(Key... keys) {
        for (Key k : keys) {
            if (s.contains(k)) {
                return true;
            }
        }
        return false;
    }
}
