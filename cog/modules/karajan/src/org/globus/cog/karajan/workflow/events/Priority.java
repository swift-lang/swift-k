//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 22, 2005
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.util.Enumerated;

public class Priority extends Enumerated {
	public static final Priority NORMAL = new Priority("NORMAL", 1);
	public static final Priority HIGH = new Priority("HIGH", 0);
	public static final Priority LOW = new Priority("LOW", 2);
	public static final int MAX = 2;

	public static final Priority[] ALL = new Priority[] { Priority.HIGH, Priority.NORMAL,
			Priority.LOW };

	private Priority(String name, int numeric) {
		super(name, numeric);
	}

	public final int getNumeric() {
		return super.getValue();
	}
}
