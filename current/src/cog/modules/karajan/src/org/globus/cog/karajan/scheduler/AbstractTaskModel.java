
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Feb 6, 2004
 *
 */
package org.globus.cog.karajan.scheduler;

import javax.swing.table.DefaultTableModel;

import org.globus.cog.abstraction.interfaces.Task;

public abstract class AbstractTaskModel extends DefaultTableModel{
	public abstract void update();
	
	public abstract Task getTaskAtRow(int row);
}
