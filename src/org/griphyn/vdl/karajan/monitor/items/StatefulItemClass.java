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
