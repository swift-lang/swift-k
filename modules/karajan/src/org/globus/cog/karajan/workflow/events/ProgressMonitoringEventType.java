//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 15, 2006
 */
package org.globus.cog.karajan.workflow.events;

public class ProgressMonitoringEventType extends MonitoringEventType {
	public static final ProgressMonitoringEventType TRANSFER_PROGRESS = new ProgressMonitoringEventType(
			"TRANSFER_PROGRESS", 0);

	public ProgressMonitoringEventType(String literal, int value) {
		super(literal, value);

	}

}
