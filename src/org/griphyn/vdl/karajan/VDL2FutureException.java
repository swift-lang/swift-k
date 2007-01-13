/*
 * Created on Jan 4, 2007
 */
package org.griphyn.vdl.karajan;

import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.griphyn.vdl.mapping.DSHandle;

public class VDL2FutureException extends RuntimeException {
	private final DSHandle handle;
	private EventListener listener;
	private Event event;

	public VDL2FutureException(DSHandle handle, EventListener listener, Event event) {
		this.handle = handle;
		this.listener = listener;
		this.event = event;
	}

	public VDL2FutureException(DSHandle handle) {
		this(handle, null, null);
	}

	public DSHandle getHandle() {
		return handle;
	}

	public Event getEvent() {
		return event;
	}

	public EventListener getListener() {
		return listener;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public void setListener(EventListener listener) {
		this.listener = listener;
	}
}
