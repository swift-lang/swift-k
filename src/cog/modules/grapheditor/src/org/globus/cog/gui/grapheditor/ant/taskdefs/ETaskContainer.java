
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant.taskdefs;

import java.util.List;

import org.apache.tools.ant.TaskContainer;

/**
 * Extends the Ant task container to allow access to the children
 */
public interface ETaskContainer extends TaskContainer{
	public List getTasks();
}

