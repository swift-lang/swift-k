
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 30, 2003
 */
package org.globus.cog.karajan.viewer;

import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.FlowEvent;

public interface HookListener {
	public void breakpointReached(ThreadedUID el, FlowEvent event);
	
	public void hookedEvent(Event e, EventListener l);
}
