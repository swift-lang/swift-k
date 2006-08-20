//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 25, 2005
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.util.Enumerated;

public final class EventClass extends Enumerated {
	public static final EventClass GENERIC_EVENT = new EventClass("GENERIC_EVENT", 0);
	public static final EventClass CONTROL_EVENT = new EventClass("CONTROL_EVENT", 1);
	public static final EventClass NOTIFICATION_EVENT = new EventClass("NOTIFICATION_EVENT", 2);
	public static final EventClass ABORT_EVENT = new EventClass("ABORT_EVENT", 3);
	public static final EventClass ARGUMENT_GENERATION_EVENT = new EventClass("ARGUMENT_GENERATION_EVENT", 4);
	public static final EventClass MONITORING_EVENT = new EventClass("MONITORING_EVENT", 5);
		
	private EventClass(String type, int value) {
		super(type, value);
	}
}
