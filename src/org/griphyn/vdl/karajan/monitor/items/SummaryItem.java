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

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.karajan.monitor.SystemState;

public class SummaryItem extends AbstractStatefulItem {
    public static final ApplicationState[] STATES = ApplicationState.enabledValues();
    
    public static ApplicationState getStateByKey(String key) {
        for (ApplicationState s : STATES) {
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
	
	public synchronized int getCount(ApplicationState key) {
        Integer i = counts.get(key.getName());
        if (i == null) {
            return 0;
        }
        else {
            return i;
        }
    }
	
	public int getCount(String key, SystemState state) {
        return getCount(key);
	}
	
	public synchronized Map<String, Integer> getCounts(SystemState state) {
        return new HashMap<String, Integer>(counts);
	}
	
	public synchronized void setCount(String key, int value) {
	    counts.put(key, value);
	}	
}
