
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.workflow.events;

import org.globus.cog.karajan.workflow.ExecutionException;

/**
 * Event hooks can be added to the {@link EventBus} in order to intercept
 * various events. This can be used to implement monitoring tools, profiling
 * tools, etc.
 * 
 * @author Mihael Hategan
 *
 */
public interface EventHook {
	void event(EventListener element, Event e) throws ExecutionException;
}
