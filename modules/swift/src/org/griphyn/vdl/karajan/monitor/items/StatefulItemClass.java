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

public class StatefulItemClass {
	public static final StatefulItemClass WORKFLOW = new StatefulItemClass("Workflows");
    public static final StatefulItemClass APPLICATION = new StatefulItemClass("Applications");
    public static final StatefulItemClass TASK = new StatefulItemClass("Tasks");
    public static final StatefulItemClass BRIDGE = new StatefulItemClass("Bridge");
    public static final StatefulItemClass HOST = new StatefulItemClass("Host");
    public static final StatefulItemClass MISC = new StatefulItemClass("Misc");
    public static final StatefulItemClass TRACE = new StatefulItemClass("Trace");
    
    private static final StatefulItemClass[] CLASSES = new StatefulItemClass[] {
    	WORKFLOW, APPLICATION, TASK, HOST
    };
    
    public static StatefulItemClass[] getClasses() {
    	return CLASSES;
    }
    
    private final String name;
    
    private StatefulItemClass(String name) {
    	this.name = name;
    }
    
    public String getName() {
    	return name;
    }

	public boolean equals(Object obj) {
		if (obj instanceof StatefulItemClass) {
			return name.equals(((StatefulItemClass) obj).name);
		}
		else {
			return false;
		}
	}
}
