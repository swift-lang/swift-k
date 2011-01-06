
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 10, 2003
 */
package org.globus.cog.karajan.viewer;

import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.NotificationEvent;

public class ListenerEventPair {
	private EventListener listener;
	private NotificationEvent event;
	
	public ListenerEventPair(EventListener element, NotificationEvent event){
		this.listener = element;
		this.event = event;
	}
	
	
	public EventListener getListener() {
		return listener;
	}

	public NotificationEvent getEvent() {
		return event;
	}

}
