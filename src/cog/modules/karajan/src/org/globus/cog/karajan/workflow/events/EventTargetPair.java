// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Nov 3, 2003
 */
package org.globus.cog.karajan.workflow.events;

import org.apache.log4j.Logger;

public final class EventTargetPair implements Runnable {
    public static final Logger logger = Logger.getLogger(EventTargetPair.class);
    
	private final EventListener target;
	private final Event event;
	public long start;

	public EventTargetPair(Event event, EventListener target) {
		this.target = target;
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

	public EventListener getTarget() {
		return target;
	}

	public String toString() {
		return event.toString() + " -> " + target.toString();
	}

	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug(getEvent() + " -> " + getTarget());
		}
		start = System.currentTimeMillis();
		EventBus.sendHooked(target, event);
		EventBus.cummulativeEventTime += System.currentTimeMillis() - start;
		start = Long.MAX_VALUE;
		EventBus.eventsDispatched++;
	}
}
