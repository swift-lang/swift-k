
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant.taskdefs;

import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;

/**
 * Sequential for ETaskContainer
 */
public class ESequential extends Sequential implements ETaskContainer {
	private List tasks;
	
	public ESequential() {
		tasks = new LinkedList();
	}
	
	public void addTask(Task nestedTask) {
		super.addTask(nestedTask);
		tasks.add(nestedTask);
	}
	
	public List getTasks() {
		return tasks;
	}
}

