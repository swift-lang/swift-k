// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.workflow.events;

public final class NotificationEventType extends EventType {
	public static final NotificationEventType EXECUTION_COMPLETED = new NotificationEventType(
			"EXECUTION_COMPLETED", 0);
	public static final NotificationEventType EXECUTION_FAILED = new NotificationEventType(
	"EXECUTION_FAILED", 1);
	public static final NotificationEventType EXECUTION_SUSPENDED = new NotificationEventType(
	"EXECUTION_SUSPENDED", 2);
	public static final NotificationEventType EXECUTION_RESTARTED = new NotificationEventType(
	"EXECUTION_RESTARTED", 3);
	public static final NotificationEventType EXECUTION_STARTED = new NotificationEventType(
	"EXECUTION_STARTED", 4);
	/*public static final NotificationEventType EXECUTION_ABORTED = new NotificationEventType(
	"EXECUTION_ABORTED", 5);*/

	public NotificationEventType(String literal, int value) {
		super(literal, value);
	}

}