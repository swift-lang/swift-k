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
 * Created on Jan 29, 2007
 */
package org.griphyn.vdl.karajan.monitor.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;

public class SummaryItem extends AbstractStatefulItem {
    public enum State {
        INITIALIZING("Initializing"), SELECTING_SITE("Selecting site", "Sel. site"), STAGE_IN("Stage in"),
        SUBMITTING("Submitting"), SUBMITTED("Submitted"), ACTIVE("Active"), STAGE_OUT("Stage out"),
        FAILED("Failed"), REPLICATING("Replicating"), FINISHED_SUCCESSFULLY("Finished successfully", "Finished");
        
        private String name, shortName;
        
        private State(String name) {
            this.name = name;
            this.shortName = name;
        }
        
        private State(String name, String shortName) {
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
    
    public static final State[] STATES = State.values();
    
    public static State getStateByKey(String key) {
        for (State s : STATES) {
            if (s.getName().equals(key)) {
                return s;
            }
        }
        return null;
    }
    
	private Map<String, Integer> counts;
	private int status;
	
	public static final String ID = "SUMMARY";
	
	public SummaryItem() {
		super(ID);
		counts = new HashMap<String, Integer>();
	}
	
	public StatefulItemClass getItemClass() {
		return StatefulItemClass.WORKFLOW;
	}

	public String toString() {
		return counts.toString();
	}

	public synchronized int getCount(String key) {
	    Integer i = counts.get(key);
	    if (i == null) {
	        return 0;
	    }
	    else {
	        return i;
	    }
	}
	
	public synchronized int getCount(State key) {
        Integer i = counts.get(key.getName());
        if (i == null) {
            return 0;
        }
        else {
            return i;
        }
    }
	
	public int getCount(String key, SystemState state) {
	    if (state.getStack() != null) {
	        // TODO Must get these from log
	        return -1;
	    }
	    else {
	        return getCount(key);
	    }
	}
	
	public synchronized Map<String, Integer> getCounts(SystemState state) {
	    if (state.getStack() != null) {
	        // TODO Must get these from log
            return Collections.emptyMap();
	    }
	    else {
	        return new HashMap<String, Integer>(counts);
	    }
	}
	
	public synchronized void setCount(String key, int value) {
	    counts.put(key, value);
	}	
}
