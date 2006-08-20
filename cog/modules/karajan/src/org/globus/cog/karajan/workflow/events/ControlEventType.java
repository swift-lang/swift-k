//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.workflow.events;


public class ControlEventType extends EventType {
	public static final ControlEventType START = new ControlEventType("START", 0);
	public static final ControlEventType RESTART = new ControlEventType("RESTART", 1);
	
	public ControlEventType(String type, int value) {
		super(type, value);
	}
}
